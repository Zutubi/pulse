package com.cinnamonbob.model;

import com.cinnamonbob.model.persistence.UserDao;

import java.util.List;

/**
 * 
 *
 */
public class DefaultUserManager implements UserManager
{
    private UserDao userDao;

    public void setUserDao(UserDao userDao)
    {
        this.userDao = userDao;
    }

    public void createNewUser(User user)
    {
        userDao.save(user);
    }

    public User getUser(String login)
    {
        return userDao.findByLogin(login);
    }

    public User getUser(long id)
    {
        return (User) userDao.findById(id);
    }

    public List<User> getAllUsers()
    {
        return userDao.findAll();
    }

    public List<User> getUsersWithLoginLike(String login)
    {
        return userDao.findByLikeLogin(login);
    }
}
