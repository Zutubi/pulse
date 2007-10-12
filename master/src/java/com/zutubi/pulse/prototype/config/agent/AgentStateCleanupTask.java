package com.zutubi.pulse.prototype.config.agent;

import com.zutubi.prototype.config.cleanup.RecordCleanupTaskSupport;
import com.zutubi.pulse.model.AgentStateManager;

/**
 * Cleans up the state associated with a deleted agent.
 */
public class AgentStateCleanupTask extends RecordCleanupTaskSupport
{
    private AgentConfiguration agentConfig;
    private AgentStateManager agentStateManager;

    public AgentStateCleanupTask(AgentConfiguration agentConfig, AgentStateManager agentStateManager)
    {
        super(agentConfig.getConfigurationPath());
        this.agentConfig = agentConfig;
        this.agentStateManager = agentStateManager;
    }

    public void run()
    {
        agentStateManager.delete(agentConfig.getAgentStateId());
    }

    public boolean isAsynchronous()
    {
        return true;
    }
}
