package com.zutubi.pulse.prototype.config.agent;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;

/**
 * Shows agent status information.
 */
public class AgentConfigurationStateDisplay
{
    private AgentManager agentManager;

    public String formatStatus(AgentConfiguration config)
    {
        Agent agent = agentManager.getAgent(config);
        return agent.getStatus().getPrettyString();
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
