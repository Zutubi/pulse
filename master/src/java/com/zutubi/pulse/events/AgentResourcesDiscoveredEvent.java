package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.SlaveAgent;

/**
 */
public class AgentResourcesDiscoveredEvent extends SlaveAgentEvent
{
    public AgentResourcesDiscoveredEvent(Object source, SlaveAgent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Agent Resources Discovered Event");
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getName());
        }
        return buff.toString();
    }
}
