package com.zutubi.pulse.events.build;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.core.model.Result;

/**
 */
public class BuildEvent extends Event<Object>
{
    private BuildResult buildResult;

    public BuildEvent(Object source, BuildResult buildResult)
    {
        super(source);
        this.buildResult = buildResult;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public Result getResult()
    {
        return buildResult;
    }

    public String toString()
    {
        return "Build Event";
    }
}
