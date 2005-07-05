package com.cinnamonbob.model;

import com.cinnamonbob.model.persistence.UserDao;

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
}
