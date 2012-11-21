package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.persistence.AgentStateDao;

/**
 * Hibernate-specific implementation of {@link AgentStateDao}.
 */
public class HibernateAgentStateDao extends HibernateEntityDao<AgentState> implements AgentStateDao
{
    public Class<AgentState> persistentClass()
    {
        return AgentState.class;
    }
}
