package com.zutubi.pulse.master.events;

import com.zutubi.events.Event;
import com.zutubi.pulse.master.agent.Agent;

/**
 * <class comment/>
 */
public class AgentEvent extends Event
{
    private Agent agent;

    public AgentEvent(Object source, Agent agent)
    {
        super(source);

        this.agent = agent;
    }

    public Agent getAgent()
    {
        return agent;
    }
}
