package com.zutubi.pulse.master.events.build;

import com.zutubi.events.Event;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.model.Result;
import com.zutubi.pulse.model.BuildResult;

/**
 */
public class BuildEvent extends Event
{
    private BuildResult buildResult;
    private ExecutionContext context;

    public BuildEvent(Object source, BuildResult buildResult, ExecutionContext context)
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

    public ExecutionContext getContext()
    {
        return context;
    }

    public String toString()
    {
        return "Build Event";
    }
}
