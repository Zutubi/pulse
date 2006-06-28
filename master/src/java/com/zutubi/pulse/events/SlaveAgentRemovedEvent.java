package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.SlaveAgent;

/**
 */
public class SlaveAgentRemovedEvent extends SlaveEvent
{
    public SlaveAgentRemovedEvent(Object source, SlaveAgent agent)
    {
        super(source, agent);
    }
}
