package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.Agent;

/**
 * Raised when the user has requested that an agent be enabled.
 */
public class AgentEnableRequestedEvent extends AgentEvent
{
    public AgentEnableRequestedEvent(Object source, Agent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        return ("Agent Enable Requested Event: " + getAgent().getName());
    }
}
