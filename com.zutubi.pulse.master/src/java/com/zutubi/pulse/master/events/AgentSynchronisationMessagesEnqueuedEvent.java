package com.zutubi.pulse.master.events;

import com.zutubi.pulse.master.agent.Agent;

/**
 * Raised when one or more synchronisation messages are enqueued for an agent.
 */
public class AgentSynchronisationMessagesEnqueuedEvent extends AgentEvent
{
    public AgentSynchronisationMessagesEnqueuedEvent(Object source, Agent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder("Agent Synchronisation Messages Enqueued Event");
        if (getAgent() != null)
        {
            builder.append(": ").append(getAgent().getName());
        }
        return builder.toString();
    }
}