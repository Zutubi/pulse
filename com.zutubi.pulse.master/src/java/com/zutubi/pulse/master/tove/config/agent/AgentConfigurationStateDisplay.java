package com.zutubi.pulse.master.tove.config.agent;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;

/**
 * Shows agent status information.
 */
public class AgentConfigurationStateDisplay
{
    private AgentManager agentManager;

    public String formatStatus(AgentConfiguration config)
    {
        Agent agent = agentManager.getAgent(config);
        if (agent == null)
        {
            return "[invalid agent]";
        }
        else
        {
            return agent.getStatus().getPrettyString();
        }
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
