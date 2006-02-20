package com.cinnamonbob.user;

import com.cinnamonbob.model.persistence.UserDao;
import com.cinnamonbob.model.persistence.ContactPointDao;
import com.cinnamonbob.model.ContactPoint;
import com.cinnamonbob.user.UserManager;
import com.cinnamonbob.user.User;

import java.util.List;

import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;

/**
 * 
 *
 */
public class DefaultUserManager implements UserManager
{
    private UserDao userDao;
    private ContactPointDao contactDao;

    public void setUserDao(UserDao userDao)
    {
        this.userDao = userDao;
    }

    public void setContactPointDao(ContactPointDao contactDao)
    {
        this.contactDao = contactDao;
    }

    public void save(User user)
    {
        userDao.save(user);
    }

    public void save(ContactPoint contact)
    {
        contactDao.save(contact);
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
        return contactDao.findById(id);
    }

    public void delete(User user)
    {
        userDao.delete(user);
    }

    public void delete(ContactPoint contact)
    {
        contactDao.delete(contact);
    }

    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessException
    {
        final User requestedUser = getUser(username);
        if (requestedUser == null)
        {
            throw new UsernameNotFoundException("Unknown username: '" + username + "'");
        }
        return requestedUser;
    }
}
