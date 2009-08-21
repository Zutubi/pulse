package com.zutubi.pulse.master.events.build;

import com.zutubi.events.Event;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.model.Result;
import com.zutubi.pulse.master.model.BuildResult;

/**
 */
public class BuildEvent extends Event
{
    private BuildResult buildResult;
    private PulseExecutionContext context;

    public BuildEvent(Object source, BuildResult buildResult, PulseExecutionContext context)
    {
        super(source);
        this.buildResult = buildResult;
        this.context = context;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public Result getResult()
    {
        return buildResult;
    }

    public long getMetaBuildId()
    {
        return buildResult.getMetaBuildId();
    }

    public PulseExecutionContext getContext()
    {
        return context;
    }

    public String toString()
    {
        return "Build Event";
    }
}
