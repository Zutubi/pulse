package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.model.BuildResult;

/**
 * This event is raised by the build processor when a build is complete, but
 * before the final indexing and saving of the build result.  Handle this
 * event for tasks that should happen after a build but that may wish to
 * modify the build result.
 */
public class PostBuildEvent extends BuildEvent
{
    public PostBuildEvent(Object source, BuildResult result, ExecutionContext context)
    {
        super(source, result, context);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Post Build Event");
        if (getBuildResult() != null)
        {
            buff.append(": ").append(getBuildResult().getId());
        }
        return buff.toString();
    }
}
