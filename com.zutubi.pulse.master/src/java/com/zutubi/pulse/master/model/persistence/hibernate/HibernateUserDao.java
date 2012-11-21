package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.persistence.UserDao;

/**
 * Hibernate-based implementation of {@link UserDao}.
 */
public class HibernateUserDao extends HibernateEntityDao<User> implements UserDao
{
    public Class<User> persistentClass()
    {
        return User.class;
    }
}
