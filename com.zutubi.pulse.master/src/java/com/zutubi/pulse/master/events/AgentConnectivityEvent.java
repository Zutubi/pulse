package com.zutubi.pulse.master.events;

import com.zutubi.pulse.master.agent.Agent;

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
