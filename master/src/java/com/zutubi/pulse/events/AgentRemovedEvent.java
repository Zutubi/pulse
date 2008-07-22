package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.SlaveAgent;

/**
 */
public class AgentRemovedEvent extends SlaveAgentEvent
{
    public AgentRemovedEvent(Object source, SlaveAgent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Agent Removed Event");
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getName());
        }
        return buff.toString();
    }
}
