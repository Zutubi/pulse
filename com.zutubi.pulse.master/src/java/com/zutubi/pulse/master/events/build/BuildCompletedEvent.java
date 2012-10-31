package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.master.model.BuildResult;

/**
 * This event is raised by the build processor when a build is completed and
 * the result is finalised.  Handle this event for tasks that run after a
 * build that do not need to modify the result.
 *
 * @see PostBuildEvent
 */
public class BuildCompletedEvent extends BuildEvent
{
    public BuildCompletedEvent(Object source, BuildResult result, PulseExecutionContext context)
    {
        super(source, result, context);
    }

    public String toString()
    {
        return "Build Completed Event: " + getBuildResult();
    }    
}
