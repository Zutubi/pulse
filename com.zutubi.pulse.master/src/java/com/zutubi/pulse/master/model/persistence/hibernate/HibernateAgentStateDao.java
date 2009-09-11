package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.persistence.AgentStateDao;

/**
 * Hibernate-specific implementation of {@link com.zutubi.pulse.master.model.persistence.AgentStateDao}.
 */
public class HibernateAgentStateDao extends HibernateEntityDao<AgentState> implements AgentStateDao
{
    public Class persistentClass()
    {
        return AgentState.class;
    }
}
