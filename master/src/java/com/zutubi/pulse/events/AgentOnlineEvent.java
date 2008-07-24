package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.Agent;

/**
 * Raised when an agent comes online.
 */
public class AgentOnlineEvent extends AgentConnectivityEvent
{
    public AgentOnlineEvent(Object source, Agent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        return ("Agent Online Event: " + getAgent().getConfig().getName());
    }
}
