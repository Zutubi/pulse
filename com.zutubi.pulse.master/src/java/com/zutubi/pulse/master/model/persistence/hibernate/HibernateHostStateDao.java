package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.HostState;
import com.zutubi.pulse.master.model.persistence.HostStateDao;

/**
 * Hibernate-specific implementation of {@link com.zutubi.pulse.master.model.persistence.HostStateDao}.
 */
public class HibernateHostStateDao extends HibernateEntityDao<HostState> implements HostStateDao
{
    public Class persistentClass()
    {
        return HostState.class;
    }
}