package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.SlaveAgent;

/**
 */
public class AgentChangedEvent extends SlaveAgentEvent
{
    public AgentChangedEvent(Object source, SlaveAgent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Agent Changed Event");
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getName());
        }
        return buff.toString();
    }
}
