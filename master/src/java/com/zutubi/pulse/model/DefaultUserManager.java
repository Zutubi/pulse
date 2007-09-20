package com.zutubi.pulse.model;

import com.zutubi.prototype.config.*;
import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.license.LicenseManager;
import com.zutubi.pulse.license.authorisation.AddUserAuthorisation;
import com.zutubi.pulse.model.persistence.UserDao;
import com.zutubi.pulse.prototype.config.group.GroupConfiguration;
import com.zutubi.pulse.prototype.config.user.DashboardConfiguration;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;
import com.zutubi.pulse.security.ldap.LdapManager;
import org.acegisecurity.providers.encoding.PasswordEncoder;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;

import java.util.*;

/**
 *
 *
 */
public class DefaultUserManager implements UserManager, ConfigurationInjector.ConfigurationSetter<User>
{
    private UserDao userDao;
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
    private ConfigurationProvider configurationProvider;
    private Map<UserConfiguration, List<GroupConfiguration>> groupsByUser;
    private Map<Long, UserConfiguration> userConfigsById = new HashMap<Long, UserConfiguration>();

    public void init()
    {
        // register the canAddUser license authorisation
        AddUserAuthorisation addUserAuthorisation = new AddUserAuthorisation();
        addUserAuthorisation.setUserManager(this);
        licenseManager.addAuthorisation(addUserAuthorisation);

        initGroupsByUser();
        initUsersById();

        configurationProvider.registerEventListener(new ConfigurationEventListener()
        {
            public void handleConfigurationEvent(ConfigurationEvent event)
            {
                initGroupsByUser();
            }
        }, true, true, ConfigurationRegistry.GROUPS_SCOPE);

        TypeListener<UserConfiguration> userListener = new TypeListener<UserConfiguration>(UserConfiguration.class)
        {
            public void postInsert(UserConfiguration instance)
            {
                User user = new User();
                userDao.save(user);
                instance.setUserId(user.getId());

                userConfigsById.put(user.getId(), instance);
            }

            public void postDelete(UserConfiguration instance)
            {
                // FIXME USERS cleanup the user and personal builds ...
                // Deleting a user removes them from groups
                initGroupsByUser();

                userConfigsById.remove(instance.getUserId());
            }

            public void postSave(UserConfiguration instance)
            {
                userConfigsById.remove(instance.getUserId());
                userConfigsById.put(instance.getUserId(), instance);
            }
        };
        userListener.register(configurationProvider);
    }

    private void initGroupsByUser()
    {
        groupsByUser = new HashMap<UserConfiguration, List<GroupConfiguration>>();
        for(GroupConfiguration group: configurationProvider.getAll(GroupConfiguration.class))
        {
            for(UserConfiguration member: group.getMembers())
            {
                List<GroupConfiguration> userGroups = groupsByUser.get(member);
                if(userGroups == null)
                {
                    userGroups = new LinkedList<GroupConfiguration>();
                    groupsByUser.put(member, userGroups);
                }

                userGroups.add(group);
            }
        }
    }

    private void initUsersById()
    {
        for(UserConfiguration user: configurationProvider.getAll(UserConfiguration.class))
        {
            userConfigsById.put(user.getUserId(), user);
        }
    }

    /**
     * @deprecated
     */
    public void save(User user)
    {
        userDao.save(user);
    }

    public UserConfiguration getUserConfig(String login)
    {
        return configurationProvider.get(PathUtils.getPath(ConfigurationRegistry.USERS_SCOPE, login), UserConfiguration.class);
    }

    public User getUser(String login)
    {
        UserConfiguration config = getUserConfig(login);
        if(config == null)
        {
            return null;
        }
        else
        {
            return userDao.findById(config.getUserId());
        }
    }

    public User getUser(long id)
    {
        return userDao.findById(id);
    }

    public List<User> getAllUsers()
    {
        return userDao.findAll();
    }

    public void delete(User user)
    {
        getBuildManager().deleteAllBuilds(user);
        userDao.delete(user);
        licenseManager.refreshAuthorisations();
    }

    public long getNextBuildNumber(User user)
    {
        user = getUser(user.getId());
        long number = user.getNextBuildNumber();
        user.setNextBuildNumber(number + 1);
        save(user);
        return number;
    }

    public GroupConfiguration getGroupConfig(String name)
    {
        return configurationProvider.get(PathUtils.getPath(ConfigurationRegistry.GROUPS_SCOPE, name), GroupConfiguration.class);
    }

    public Set<Project> getUserProjects(User user, final ProjectManager projectManager)
    {
        Set<Project> projects = new HashSet<Project>();
        DashboardConfiguration dashboardConfig = user.getConfig().getPreferences().getDashboard();
        if(dashboardConfig.isShowAllProjects())
        {
            projects.addAll(projectManager.getProjects(true));
        }
        else
        {
            projects.addAll(projectManager.mapConfigsToProjects(dashboardConfig.getShownProjects()));
            List<String> groupNames = user.getPreferences().getDashboard().getShownGroups();
            for(String groupName: groupNames)
            {
                ProjectGroup group = projectManager.getProjectGroup(groupName);
                projects.addAll(group.getProjects());
            }
        }

        return projects;
    }

    public AcegiUser getPrinciple(User user)
    {
        AcegiUser principle = new AcegiUser(user, groupsByUser.get(user.getConfig()));
        getLdapManager().addLdapRoles(principle);
        return principle;
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
        User user = getUser(username);
        if (user == null)
        {
            throw new UsernameNotFoundException("Unknown user");
        }

        return new AcegiUser(user, groupsByUser.get(user.getConfig()));
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
        user.getConfig().setPassword(encodedPassword);
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }

    public void setUserDao(UserDao userDao)
    {
        this.userDao = userDao;
    }

    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
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

    public void setConfiguration(User state)
    {
        state.setConfig(userConfigsById.get(state.getId()));
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setConfigurationInjector(ConfigurationInjector configurationInjector)
    {
        configurationInjector.registerSetter(User.class, this);
    }
}
