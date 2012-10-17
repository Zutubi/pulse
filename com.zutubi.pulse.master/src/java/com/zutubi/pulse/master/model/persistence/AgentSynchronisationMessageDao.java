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
     * Finds all messages with the given status, across all agents.
     *
     * @param status the status to find messages in
     * @return all messages with the given status
     */
    List<AgentSynchronisationMessage> findByStatus(AgentSynchronisationMessage.Status status);

    /**
     * Finds all messages for a given agent with a given status and task type.
     *
     * @param agentState  state of the agent to get messages for
     * @param status      status to filter by
     * @param taskType    type of tasks to filter by
     * @return all messages for the given agent that meet the criteria
     */
    List<AgentSynchronisationMessage> queryMessages(AgentState agentState, AgentSynchronisationMessage.Status status, String taskType);

    /**
     * Deleted all messages for the given agent.
     *
     * @param agentState state of the agent to delete the messages for
     */
    int deleteByAgentState(AgentState agentState);
}