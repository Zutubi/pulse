package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.AgentState;
import com.zutubi.pulse.model.PersistentResource;

import java.util.List;

/**
 */
public interface ResourceDao extends EntityDao
{
    List<PersistentResource> findAllBySlave(AgentState agentState);
    PersistentResource findBySlaveAndName(AgentState agentState, String name);
}
