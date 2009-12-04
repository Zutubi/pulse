package com.zutubi.pulse.master;

import com.zutubi.events.Event;
import com.zutubi.pulse.master.scheduling.EventTriggerFilter;
import com.zutubi.pulse.master.scheduling.TaskExecutionContext;
import com.zutubi.pulse.master.scheduling.Trigger;

/**
 * Disable the dependent build event triggering since this is handled by the 
 * build scheduling system.  The trigger configuration is still in place and
 * used to mark that we want the dependency traversed, but the actual triggering
 * of the build is no longer handled by this event.
 *
 * @deprecated retained for backwards compatibility and to allow us to retain
 * the trigger configuration for this type of build.
 */
public class DependentBuildEventFilter implements EventTriggerFilter
{
    public boolean accept(Trigger trigger, Event event, TaskExecutionContext context)
    {
        return false;
    }
}
