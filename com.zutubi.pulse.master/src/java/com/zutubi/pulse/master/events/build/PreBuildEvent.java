package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.model.BuildResult;

/**
 * This event is raised by the build controller just before it commences a
 * build.  Handle this event for tasks that should be run before the build
 * controller actually begins the build process.
 */
public class PreBuildEvent extends BuildEvent
{
    public PreBuildEvent(Object source, BuildResult result, ExecutionContext context)
    {
        super(source, result, context);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Pre Build Event");
        if (getBuildResult() != null)
        {
            buff.append(": ").append(getBuildResult().getId());
        }
        return buff.toString();
    }
}
