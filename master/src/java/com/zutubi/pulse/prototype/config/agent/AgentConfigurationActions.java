package com.zutubi.pulse.prototype.config.agent;

import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.model.AgentState;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class AgentConfigurationActions
{
    private AgentManager agentManager;

    public List<String> getActions(AgentConfiguration config)
    {
        List<String> actions = new LinkedList<String>();

        Agent agent = agentManager.getAgent(config.getHandle());
        if (agent.isEnabled())
        {
            actions.add("disable");
            actions.add("ping");
        }
        else if (agent.isDisabled())
        {
            actions.add("enable");
        }

        return actions;
    }

    public void doDisable(AgentConfiguration config)
    {
        agentManager.setAgentState(config.getHandle(), AgentState.EnableState.DISABLED);
    }

    public void doEnable(AgentConfiguration config)
    {
        agentManager.setAgentState(config.getHandle(), AgentState.EnableState.ENABLED);
    }

    public void doPing(AgentConfiguration config)
    {
        agentManager.pingAgent(config.getHandle());
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
