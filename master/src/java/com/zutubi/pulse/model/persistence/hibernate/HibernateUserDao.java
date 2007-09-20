package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.persistence.UserDao;

/**
 */
@SuppressWarnings({ "unchecked" })
public class HibernateUserDao extends HibernateEntityDao<User> implements UserDao
{
    public Class persistentClass()
    {
        return User.class;
    }
}
