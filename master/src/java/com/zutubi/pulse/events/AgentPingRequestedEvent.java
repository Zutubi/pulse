package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.SlaveAgent;

/**
 * Raised when an agent should be pinged ASAP.
 */
public class AgentPingRequestedEvent extends SlaveAgentEvent
{
    public AgentPingRequestedEvent(Object source, SlaveAgent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        return ("Agent Ping Requested Event: " + getAgent().getName());
    }
}
