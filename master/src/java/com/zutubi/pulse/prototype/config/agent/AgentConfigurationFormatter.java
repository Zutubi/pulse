package com.zutubi.pulse.prototype.config.agent;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;

/**
 *
 *
 */
public class AgentConfigurationFormatter
{
    private AgentManager agentManager;

    public String getLocation(AgentConfiguration configuration)
    {
        Agent agentState = agentManager.getAgent(configuration.getHandle());
        return agentState.getLocation();
    }

    public String getStatus(AgentConfiguration configuration)
    {
        //TODO: make this I18N'ed

        Agent agentState = agentManager.getAgent(configuration.getHandle());
        if (agentState.isEnabled())
        {
            return agentState.getStatus().getPrettyString();
        }
        else
        {
            return agentState.getEnableState().toString();
        }
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
