package com.zutubi.pulse.events;

import com.zutubi.pulse.master.agent.Agent;

/**
 * Raised when an Agent is added (not the configuration, the transient
 * state object).
 */
public class AgentAddedEvent extends AgentEvent
{
    public AgentAddedEvent(Object source, Agent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Agent Added Event");
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getConfig().getName());
        }
        return buff.toString();
    }
}
