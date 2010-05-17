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
        StringBuffer buff = new StringBuffer("Agent Synchronisation Messages Enqueued Event");
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getConfig().getName());
        }
        return buff.toString();
    }
}