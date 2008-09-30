package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.Agent;

/**
 */
public class AgentResourcesDiscoveredEvent extends AgentEvent
{
    public AgentResourcesDiscoveredEvent(Object source, Agent agent)
    {
        super(source, agent);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Agent Resources Discovered Event");
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getConfig().getName());
        }
        return buff.toString();
    }
}
