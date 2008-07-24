package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.Agent;

/**
 * Raised when an agent should be pinged ASAP.
 */
public class AgentPingRequestedEvent extends AgentEvent
{
    public AgentPingRequestedEvent(Object source, Agent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        return ("Agent Ping Requested Event: " + getAgent().getConfig().getName());
    }
}
