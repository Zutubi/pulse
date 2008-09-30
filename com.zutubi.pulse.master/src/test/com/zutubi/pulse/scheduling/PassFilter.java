package com.zutubi.pulse.scheduling;

import com.zutubi.events.Event;

/**
 */
public class PassFilter implements EventTriggerFilter
{
    public boolean accept(Trigger trigger, Event event, TaskExecutionContext context)
    {
        return true;
    }

    public boolean dependsOnProject(Trigger trigger, long projectId)
    {
        return false;
    }
}
