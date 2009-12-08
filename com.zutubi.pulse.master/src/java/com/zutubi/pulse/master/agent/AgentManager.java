package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.agent.statistics.AgentStatistics;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.security.SecureParameter;
import com.zutubi.pulse.master.security.SecureResult;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfigurationActions;
import com.zutubi.tove.security.AccessManager;

import java.util.List;

/**
 */
public interface AgentManager extends AgentPersistentStatusManager
{
    String GLOBAL_AGENT_NAME = "global agent template";
    String MASTER_AGENT_NAME = "master";

    @SecureResult
    List<Agent> getAllAgents();
    @SecureResult
    List<Agent> getOnlineAgents();
    @SecureResult
    List<Agent> getAvailableAgents();
    @SecureResult
    Agent getAgent(long handle);
    @SecureResult
    Agent getAgent(AgentConfiguration agent);
    @SecureResult
    Agent getAgent(String name);

    @SecureParameter(parameterType = AgentConfiguration.class, action = AccessManager.ACTION_DELETE)
    void deleteState(AgentState state);

    @SecureParameter(parameterType = AgentConfiguration.class, action = AgentConfigurationActions.ACTION_PING)
    void pingAgent(AgentConfiguration agentConfig);

    int getAgentCount();

    /**
     * Returns statistics gathered for the given agent.
     *
     * @param agent the agent to get statistics for
     * @return statistics for the given agent
     */
    @SecureParameter(parameterType = Agent.class, action = AccessManager.ACTION_VIEW)
    AgentStatistics getAgentStatistics(Agent agent);

    /**
     * Instructs the manager to update the agent statistics for this point in
     * time.  Should be called regularly to keep statistics current.
     *
     * @see com.zutubi.pulse.master.agent.statistics.UpdateStatisticsTask
     */
    void updateStatistics();
}
