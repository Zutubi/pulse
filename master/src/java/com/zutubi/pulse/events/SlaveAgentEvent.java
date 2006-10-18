package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.SlaveAgent;

/**
 */
public class SlaveAgentEvent extends AgentEvent
{
    private SlaveAgent agent;

    public SlaveAgentEvent(Object source, SlaveAgent agent)
    {
        super(source, agent);
        this.agent = agent;
    }

    public SlaveAgent getSlaveAgent()
    {
        return agent;
    }

    public String toString()
    {
        return "Agent Event";
    }    
}
