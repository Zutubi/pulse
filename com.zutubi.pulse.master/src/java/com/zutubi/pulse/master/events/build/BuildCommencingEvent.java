package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.master.model.BuildResult;

/**
 * An event raised when a build is about to commence.  Raised just before the revision is finalised and dependencies are
 * resolved for the build.
 */
public class BuildCommencingEvent extends BuildEvent
{
    public BuildCommencingEvent(Object source, BuildResult buildResult, PulseExecutionContext context)
    {
        super(source, buildResult, context);
    }

    @Override
    public String toString()
    {
        return "Build Commencing Event: " + getBuildResult();
    }
}
