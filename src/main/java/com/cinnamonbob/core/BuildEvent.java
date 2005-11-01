package com.cinnamonbob.core;

import com.cinnamonbob.event.Event;
import com.cinnamonbob.model.BuildResult;

/**
 */
public class BuildEvent extends Event
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
