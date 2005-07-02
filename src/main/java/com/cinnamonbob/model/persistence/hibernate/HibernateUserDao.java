package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.persistence.UserDao;
import com.cinnamonbob.model.User;

/**
 * 
 *
 */
public class HibernateUserDao extends HibernateEntityDao implements UserDao
{
    public Class persistentClass()
    {
        return User.class;
    }

    
}
