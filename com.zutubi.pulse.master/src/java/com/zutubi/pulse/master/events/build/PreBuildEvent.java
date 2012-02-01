package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.master.model.BuildResult;

/**
 * This event is raised by the build controller just before it commences a
 * build.  Handle this event for tasks that should be run before the build
 * controller actually begins the build process.
 */
public class PreBuildEvent extends BuildEvent
{
    public PreBuildEvent(Object source, BuildResult result, PulseExecutionContext context)
    {
        super(source, result, context);
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder("Pre Build Event");
        if (getBuildResult() != null)
        {
            builder.append(": ").append(getBuildResult().getId());
        }
        return builder.toString();
    }
}
