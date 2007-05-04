package com.zutubi.pulse.model;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.license.LicenseManager;
import com.zutubi.pulse.license.authorisation.AddUserAuthorisation;
import com.zutubi.pulse.model.persistence.ContactPointDao;
import com.zutubi.pulse.model.persistence.GroupDao;
import com.zutubi.pulse.model.persistence.UserDao;
import com.zutubi.pulse.security.ldap.LdapManager;
import com.zutubi.pulse.web.DefaultAction;
import org.acegisecurity.providers.encoding.PasswordEncoder;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 *
 */
public class DefaultUserManager implements UserManager
{
    private UserDao userDao;
    private ContactPointDao contactPointDao;
    private GroupDao groupDao;
    private PasswordEncoder passwordEncoder;

    private LicenseManager licenseManager;
    /**
     * Do not access directly, always use getBuildManager().  This dependency
     * is initialised on demand (not available when this manager is created).
     */
    private BuildManager buildManager;
    /**
     * Do not access directly, always use getLdapManager().  This dependency
     * is initialised on demand (not available when this manager is created).
     */
    private LdapManager ldapManager;

    public void init()
    {
        // register the canAddUser license authorisation
        AddUserAuthorisation addUserAuthorisation = new AddUserAuthorisation();
        addUserAuthorisation.setUserManager(this);
        licenseManager.addAuthorisation(addUserAuthorisation);
    }

    public void save(User user)
    {
        userDao.save(user);
    }

    public void save(ContactPoint contact)
    {
        contactPointDao.save(contact);
    }

    public boolean hasAuthority(User user, String authority)
    {
        if(user.hasAuthority(authority))
        {
            return true;
        }

        List<Group> groups = groupDao.findByMember(user);
        for(Group g: groups)
        {
            if(g.hasAuthority(authority))
            {
                return true;
            }
        }

        return false;
    }

    public void addUser(User newUser, boolean grantAdminPermissions, boolean useLdapAuthencation)
    {
        // ensure that the user has the correct authorities to login.
        newUser.add(GrantedAuthority.USER);
        if (grantAdminPermissions)
        {
            newUser.add(GrantedAuthority.ADMINISTRATOR);
        }
        newUser.setEnabled(true);
        newUser.setDefaultAction(DefaultAction.WELCOME_ACTION);
        newUser.setLdapAuthentication(useLdapAuthencation);
        save(newUser);
        // can only update the password on a persistent user since the password salt relies
        // upon the users id.
        setPassword(newUser, newUser.getPassword());
        save(newUser);

        licenseManager.refreshAuthorisations();
    }

    public User getUser(String login)
    {
        return userDao.findByLogin(login);
    }

    public User getUser(long id)
    {
        return userDao.findById(id);
    }

    public List<User> getUsersWithLoginLike(String login)
    {
        return userDao.findByLikeLogin(login);
    }

    public List<User> getAllUsers()
    {
        return userDao.findAll();
    }

    public ContactPoint getContactPoint(long id)
    {
        return contactPointDao.findById(id);
    }

    public void delete(User user)
    {
        getBuildManager().deleteAllBuilds(user);

        List<Group> groups = groupDao.findByMember(user);
        for(Group group: groups)
        {
            group.getUsers().remove(user);
            groupDao.save(group);
        }
        userDao.delete(user);

        licenseManager.refreshAuthorisations();
    }

    public void delete(ContactPoint contact)
    {
        contactPointDao.delete(contact);
    }

    public List<Group> getAllGroups()
    {
        return groupDao.findAll();
    }

    public List<Group> getAdminAllProjectGroups()
    {
        return groupDao.findByAdminAllProjects();
    }

    public Group getGroup(long id)
    {
        return groupDao.findById(id);
    }

    public Group getGroup(String name)
    {
        return groupDao.findByName(name);
    }

    public void addGroup(Group group)
    {
        groupDao.save(group);
    }

    public void save(Group group)
    {
        groupDao.save(group);
    }

    public void renameGroup(Group group, String newName)
    {
        group.setName(newName);
        groupDao.save(group);
    }

    public void delete(Group group, ProjectManager projectManager)
    {
        // We need to remove all ACLs with this group as a recipient
        projectManager.removeAcls(group.getDefaultAuthority());
        groupDao.delete(group);
    }

    public List<User> getUsersNotInGroup(Group group)
    {
        return userDao.findByNotInGroup(group);
    }

    public long getNextBuildNumber(User user)
    {
        user = getUser(user.getId());
        long number = user.getNextBuildNumber();
        user.setNextBuildNumber(number + 1);
        save(user);
        return number;
    }

    public Set<Project> getUserProjects(User user, ProjectManager projectManager)
    {
        Set<Project> projects = new HashSet<Project>();
        if(user.getShowAllProjects())
        {
            projects.addAll(projectManager.getProjects());
        }
        else
        {
            // Reload the user so we get the lazy-loaded projects
            user = userDao.findById(user.getId());
            projects.addAll(user.getShownProjects());
            for(ProjectGroup g: user.getShownGroups())
            {
                projects.addAll(g.getProjects());
            }
        }

        return projects;
    }

    public AcegiUser getPrinciple(User user)
    {
        AcegiUser principle = new AcegiUser(user);
        // Force initialisation of groups
        user.getGroups();
        getLdapManager().addLdapRoles(principle);
        return principle;
    }

    public void removeReferencesToProject(Project project)
    {
        List<User> users = userDao.findByShownProject(project);
        for(User u: users)
        {
            u.getShownProjects().remove(project);
            userDao.save(u);
        }
    }

    public void removeReferencesToProjectGroup(ProjectGroup projectGroup)
    {
        List<User> users = userDao.findByShownProjectGroup(projectGroup);
        for(User u: users)
        {
            u.getShownGroups().remove(projectGroup);
            userDao.save(u);
        }
    }

    public int getUserCount()
    {
        return userDao.count();
    }

    /**
     * Only for use by Acegi.  Calling this method directly is dangerous, as
     * the returned details are not fully initialised!  Use #getUserDetails
     * instead.
     *
     * @param username
     * @return
     * @throws UsernameNotFoundException
     * @throws DataAccessException
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException
    {
        User user = userDao.findByLogin(username);
        if (user == null)
        {
            throw new UsernameNotFoundException("Unknown user");
        }

        UserDetails details = new AcegiUser(user);
        return details;
    }

    /**
     * Update the password for this user.
     *
     * @param user
     * @param rawPassword
     */
    public void setPassword(User user, String rawPassword)
    {
        String encodedPassword = passwordEncoder.encodePassword(rawPassword, null);
        user.setPassword(encodedPassword);
    }

    /**
     * Required resource.
     *
     * @param passwordEncoder
     */
    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }

    public void setUserDao(UserDao userDao)
    {
        this.userDao = userDao;
    }

    public void setContactPointDao(ContactPointDao contactDao)
    {
        this.contactPointDao = contactDao;
    }

    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
    }

    public void setGroupDao(GroupDao groupDao)
    {
        this.groupDao = groupDao;
    }

    public BuildManager getBuildManager()
    {
        if(buildManager == null)
        {
            buildManager = (BuildManager) ComponentContext.getBean("buildManager");
        }
        return buildManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public LdapManager getLdapManager()
    {
        if(ldapManager == null)
        {
            ldapManager = (LdapManager) ComponentContext.getBean("ldapManager");
        }
        return ldapManager;
    }

    public void setLdapManager(LdapManager ldapManager)
    {
        this.ldapManager = ldapManager;
    }
}
