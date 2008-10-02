package com.zutubi.pulse.events;

import com.zutubi.pulse.master.agent.Agent;

/**
 * Raised when an agent becomes available for builds.
 */
public class AgentAvailableEvent extends AgentAvailabilityEvent
{
    public AgentAvailableEvent(Object source, Agent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        return ("Agent Available Event: " + getAgent().getConfig().getName());
    }
}
