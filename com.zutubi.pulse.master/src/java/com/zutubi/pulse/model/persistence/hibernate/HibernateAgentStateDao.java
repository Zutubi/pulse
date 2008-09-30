package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.AgentState;
import com.zutubi.pulse.model.persistence.AgentStateDao;

/**
 */
public class HibernateAgentStateDao extends HibernateEntityDao<AgentState> implements AgentStateDao
{
    public Class persistentClass()
    {
        return AgentState.class;
    }
}
