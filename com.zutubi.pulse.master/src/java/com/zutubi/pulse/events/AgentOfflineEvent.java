package com.zutubi.pulse.events;

import com.zutubi.pulse.master.agent.Agent;

/**
 * Raised when an agent goes offline.
 */
public class AgentOfflineEvent extends AgentConnectivityEvent
{
    public AgentOfflineEvent(Object source, Agent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        return ("Agent Offline Event: " + getAgent().getConfig().getName());
    }
}
