package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.SlaveAgent;

/**
 */
public class AgentAddedEvent extends SlaveAgentEvent
{
    public AgentAddedEvent(Object source, SlaveAgent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Agent Added Event");
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getName());
        }
        return buff.toString();
    }
}
