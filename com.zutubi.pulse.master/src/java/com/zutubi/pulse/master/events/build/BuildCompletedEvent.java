package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.model.BuildResult;

/**
 * This event is raised by the build processor when a build is completed and
 * the result is finalised.  Handle this event for tasks that run after a
 * build that do not need to modify the result.
 *
 * @see PostBuildEvent
 */
public class BuildCompletedEvent extends BuildEvent
{
    public BuildCompletedEvent(Object source, BuildResult result, ExecutionContext context)
    {
        super(source, result, context);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Build Completed Event");
        if (getBuildResult() != null)
        {
            buff.append(": ").append(getBuildResult().getId());
        }
        return buff.toString();
    }    
}
