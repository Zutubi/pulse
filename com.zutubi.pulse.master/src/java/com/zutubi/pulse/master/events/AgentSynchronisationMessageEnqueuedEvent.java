package com.zutubi.pulse.master.events;

import com.zutubi.pulse.master.agent.Agent;

/**
 * Raised when a synchronisation message is enqueued for an agent.
 */
public class AgentSynchronisationMessageEnqueuedEvent extends AgentEvent
{
    public AgentSynchronisationMessageEnqueuedEvent(Object source, Agent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Agent Synchronisation Message Enqueued Event");
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getConfig().getName());
        }
        return buff.toString();
    }
}