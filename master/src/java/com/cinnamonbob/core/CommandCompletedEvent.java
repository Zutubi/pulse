package com.cinnamonbob.core;

import com.cinnamonbob.core.model.BuildResult;

/**
 */
public class CommandCompletedEvent extends BuildEvent
{
    public CommandCompletedEvent(Object source, BuildResult result)
    {
        super(source, result);
    }
}
