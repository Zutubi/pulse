package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.SlaveAgent;

/**
 */
public class SlaveEvent extends Event
{
    private SlaveAgent agent;

    public SlaveEvent(Object source, SlaveAgent agent)
    {
        super(source);
        this.agent = agent;
    }

    public SlaveAgent getAgent()
    {
        return agent;
    }
}
