package com.zutubi.pulse.tove.config.agent;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;

/**
 *
 *
 */
public class AgentConfigurationFormatter
{
    private AgentManager agentManager;

    public String getLocation(AgentConfiguration configuration)
    {
        Agent agentState = agentManager.getAgent(configuration);
        return agentState.getLocation();
    }

    public String getStatus(AgentConfiguration configuration)
    {
        //TODO: make this I18N'ed

        Agent agentState = agentManager.getAgent(configuration);
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
