package com.zutubi.pulse.events.build;

import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.model.BuildResult;

/**
 * This event is raised by the build processor when commencing a build.  It
 * should only be used by the processor, to perform actions before a build
 * handle the {@link PreBuildEvent}.
 */
public class BuildCommencedEvent extends BuildEvent
{
    public BuildCommencedEvent(Object source, BuildResult result, ExecutionContext context)
    {
        super(source, result, context);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Build Commenced Event");
        if (getBuildResult() != null)
        {
            buff.append(": ").append(getBuildResult().getId());
        }
        return buff.toString();
    }    
}
