package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.Agent;

/**
 * Raised when an existing Agent is refreshed, usually due to a configuration
 * change.
 */
public class AgentChangedEvent extends AgentEvent
{
    public AgentChangedEvent(Object source, Agent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Agent Changed Event");
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getConfig().getName());
        }
        return buff.toString();
    }
}
