package com.zutubi.pulse.tove.config.agent;

import com.zutubi.config.annotations.Permission;
import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.model.AgentState;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class AgentConfigurationActions
{
    public static final String ACTION_DISABLE = "disable";
    public static final String ACTION_ENABLE = "enable";
    public static final String ACTION_GC = "gc";
    public static final String ACTION_PING = "ping";

    private AgentManager agentManager;

    public List<String> getActions(AgentConfiguration config)
    {
        List<String> actions = new LinkedList<String>();

        Agent agent = agentManager.getAgent(config);
        if (agent.isEnabled())
        {
            actions.add(ACTION_DISABLE);
            actions.add(ACTION_PING);
            actions.add(ACTION_GC);
        }
        else if (agent.isDisabled())
        {
            actions.add(ACTION_ENABLE);
        }

        return actions;
    }

    @Permission(ACTION_DISABLE)
    public void doDisable(AgentConfiguration config)
    {
        agentManager.setAgentState(config, AgentState.EnableState.DISABLED);
    }

    @Permission(ACTION_DISABLE)
    public void doEnable(AgentConfiguration config)
    {
        agentManager.setAgentState(config, AgentState.EnableState.ENABLED);
    }

    @Permission(ACTION_PING)
    public void doPing(AgentConfiguration config)
    {
        agentManager.pingAgent(config);
    }

    public void doGc(AgentConfiguration config)
    {
        Agent agent = agentManager.getAgent(config);
        if (agent != null)
        {
            agent.getService().garbageCollect();
        }
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }
}
