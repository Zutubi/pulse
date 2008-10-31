package com.zutubi.pulse.master.events;

import com.zutubi.pulse.master.agent.Agent;

/**
 * Base for availability events to ease handling.
 */
public abstract class AgentAvailabilityEvent extends AgentEvent
{
    public AgentAvailabilityEvent(Object source, Agent agent)
    {
        super(source, agent);
    }
}
