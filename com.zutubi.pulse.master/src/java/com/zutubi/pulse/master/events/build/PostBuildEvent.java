package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.master.model.BuildResult;

/**
 * This event is raised by the build processor when a build is complete, but
 * before the final indexing and saving of the build result.  Handle this
 * event for tasks that should happen after a build but that may wish to
 * modify the build result.
 */
public class PostBuildEvent extends BuildEvent
{
    public PostBuildEvent(Object source, BuildResult result, PulseExecutionContext context)
    {
        super(source, result, context);
    }

    public String toString()
    {
        return "Post Build Event: " + getBuildResult();
    }
}
