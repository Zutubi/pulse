package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.Agent;

/**
 * Abstract base for agent on/offline events for ease of handling.
 */
public abstract class AgentConnectivityEvent extends AgentEvent
{
    public AgentConnectivityEvent(Object source, Agent agent)
    {
        super(source, agent);
    }
}
