package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.persistence.UserDao;

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
