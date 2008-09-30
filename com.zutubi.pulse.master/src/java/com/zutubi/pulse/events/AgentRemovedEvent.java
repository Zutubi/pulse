package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.Agent;

/**
 * Raised when an Agent is removed (not the configuration, the transient
 * state object).
 */
public class AgentRemovedEvent extends AgentEvent
{
    public AgentRemovedEvent(Object source, Agent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Agent Removed Event");
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getConfig().getName());
        }
        return buff.toString();
    }
}
