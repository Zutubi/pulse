package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.pulse.master.model.AgentStateManager;
import com.zutubi.pulse.master.tove.config.DatabaseStateCleanupTaskSupport;
import com.zutubi.pulse.master.util.TransactionContext;

/**
 * Cleans up the state associated with a deleted agent.
 */
public class AgentStateCleanupTask extends DatabaseStateCleanupTaskSupport
{
    private AgentConfiguration agentConfig;
    private AgentStateManager agentStateManager;

    public AgentStateCleanupTask(AgentConfiguration agentConfig, AgentStateManager agentStateManager, TransactionContext transactionContext)
    {
        super(agentConfig.getConfigurationPath(), transactionContext);
        this.agentConfig = agentConfig;
        this.agentStateManager = agentStateManager;
    }

    public void cleanupState()
    {
        agentStateManager.delete(agentConfig.getAgentStateId());
    }
}
