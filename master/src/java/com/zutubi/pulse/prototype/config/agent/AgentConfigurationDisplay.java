package com.zutubi.pulse.prototype.config.agent;

import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.agent.Agent;

/**
 *
 *
 */
public class AgentConfigurationDisplay
{
    private AgentManager agentManager;

    public String getStatus(AgentConfiguration config)
    {
        Agent agent = agentManager.getAgent(config.getHandle());
        return agent.getStatus().getPrettyString();
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
