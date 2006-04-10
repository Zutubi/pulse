package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.persistence.SlaveDao;

/**
 */
public class HibernateSlaveDao extends HibernateEntityDao<Slave> implements SlaveDao
{
    public Class persistentClass()
    {
        return Slave.class;
    }

    public Slave findByName(final String name)
    {
        return (Slave) findUniqueByNamedQuery("findSlaveByName", "name", name, true);
    }

}
