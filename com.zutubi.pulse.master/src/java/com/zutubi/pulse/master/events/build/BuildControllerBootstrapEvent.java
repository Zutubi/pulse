package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.master.model.BuildResult;

/**
 * This event is raised by the build processor to bootstrap its internal
 * listener.  It should only be used by the processor, to perform actions
 * before a build handle the {@link PreBuildEvent}.
 */
public class BuildControllerBootstrapEvent extends BuildEvent
{
    private Exception startupException;

    public BuildControllerBootstrapEvent(Object source, BuildResult result, PulseExecutionContext context)
    {
        super(source, result, context);
    }

    public BuildControllerBootstrapEvent(Object source, BuildResult result, PulseExecutionContext context, Exception e)
    {
        super(source, result, context);
        this.startupException = e;
    }

    public boolean hasStartupException()
    {
        return this.startupException != null;
    }

    public Exception getStartupException()
    {
        return startupException;
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
