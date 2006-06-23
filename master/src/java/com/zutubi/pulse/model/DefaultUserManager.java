package com.zutubi.pulse.model;

import com.zutubi.pulse.model.persistence.ContactPointDao;
import com.zutubi.pulse.model.persistence.UserDao;
import com.zutubi.pulse.web.DefaultAction;
import org.acegisecurity.providers.encoding.PasswordEncoder;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;

import java.util.List;

/**
 * 
 *
 */
public class DefaultUserManager implements UserManager
{
    private UserDao userDao;
    private ContactPointDao contactPointDao;

    private PasswordEncoder passwordEncoder;

    public void setUserDao(UserDao userDao)
    {
        this.userDao = userDao;
    }

    public void setContactPointDao(ContactPointDao contactDao)
    {
        this.contactPointDao = contactDao;
    }

    public void save(User user)
    {
        userDao.save(user);
    }

    public void save(ContactPoint contact)
    {
        contactPointDao.save(contact);
    }

    public List<Project> getDashboardProjects(User user)
    {
        return userDao.getProjects(user);
    }

    public void addUser(User newUser, boolean grantAdminPermissions)
    {
        addUser(newUser, grantAdminPermissions, false);
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
        userDao.delete(user);
    }

    public void delete(ContactPoint contact)
    {
        contactPointDao.delete(contact);
    }

    public int getUserCount()
    {
        return userDao.count();
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException
    {
        UserDetails details = userDao.findByLogin(username);
        if (details == null)
        {
            throw new UsernameNotFoundException("Unknown user");
        }

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
}
