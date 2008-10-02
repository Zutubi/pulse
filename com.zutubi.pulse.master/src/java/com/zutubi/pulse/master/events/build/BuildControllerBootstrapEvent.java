package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.master.model.BuildResult;

/**
 * This event is raised by the build processor to bootstrap its internal
 * listener.  It should only be used by the processor, to perform actions
 * before a build handle the {@link PreBuildEvent}.
 */
public class BuildControllerBootstrapEvent extends BuildEvent
{
    public BuildControllerBootstrapEvent(Object source, BuildResult result, ExecutionContext context)
    {
        super(source, result, context);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Build Controller Bootstrap Event");
        if (getBuildResult() != null)
        {
            buff.append(": ").append(getBuildResult().getId());
        }
        return buff.toString();
    }    
}
