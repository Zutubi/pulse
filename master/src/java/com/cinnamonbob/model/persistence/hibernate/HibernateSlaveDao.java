package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.Slave;
import com.cinnamonbob.model.persistence.SlaveDao;

/**
 */
public class HibernateSlaveDao extends HibernateEntityDao<Slave> implements SlaveDao
{
    public Class persistentClass()
    {
        return Slave.class;
    }
}
