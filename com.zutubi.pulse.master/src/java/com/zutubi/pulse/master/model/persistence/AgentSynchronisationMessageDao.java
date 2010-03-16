package com.zutubi.pulse.master.model.persistence;

import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.AgentSynchronisationMessage;

import java.util.List;

/**
 * Provides access to agent synchronisation message entities.
 */
public interface AgentSynchronisationMessageDao extends EntityDao<AgentSynchronisationMessage>
{
    /**
     * Finds all messages for the given agent.
     *
     * @param agentState state of the agent to get the messages for
     * @return all messages for the specified agent
     */
    List<AgentSynchronisationMessage> findByAgentState(AgentState agentState);

    /**
     * Deleted all messages for the given agent.
     *
     * @param agentState state of the agent to delete the messages for
     */
    int deleteByAgentState(AgentState agentState);
}