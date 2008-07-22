package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.Agent;

/**
 * Raised when the user has requested that an agent be disabled.
 */
public class AgentDisableRequestedEvent extends AgentEvent
{
    public AgentDisableRequestedEvent(Object source, Agent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        return ("Agent Disable Requested Event: " + getAgent().getName());
    }
}
