package com.zutubi.pulse.master.scheduling;

import com.zutubi.events.Event;

/**
 */
public class PassFilter implements EventTriggerFilter
{
    public boolean accept(Trigger trigger, Event event, TaskExecutionContext context)
    {
        return true;
    }
}
