package com.zutubi.pulse.events.build;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.model.BuildResult;

/**
 */
public class BuildEvent extends Event<Object>
{
    private BuildResult result;

    public BuildEvent(Object source, BuildResult result)
    {
        super(source);
        this.result = result;
    }

    public BuildResult getResult()
    {
        return result;
    }
}
