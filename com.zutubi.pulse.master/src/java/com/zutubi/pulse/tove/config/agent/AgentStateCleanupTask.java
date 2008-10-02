package com.zutubi.pulse.tove.config.agent;

import com.zutubi.pulse.master.model.AgentStateManager;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.tove.config.DatabaseStateCleanupTaskSupport;

/**
 * Cleans up the state associated with a deleted agent.
 */
public class AgentStateCleanupTask extends DatabaseStateCleanupTaskSupport
{
    private AgentConfiguration agentConfig;
    private AgentStateManager agentStateManager;

    public AgentStateCleanupTask(AgentConfiguration agentConfig, AgentStateManager agentStateManager, BuildManager buildManager)
    {
        super(agentConfig.getConfigurationPath(), buildManager);
        this.agentConfig = agentConfig;
        this.agentStateManager = agentStateManager;
    }

    public void cleanupState()
    {
        agentStateManager.delete(agentConfig.getAgentStateId());
    }
}
