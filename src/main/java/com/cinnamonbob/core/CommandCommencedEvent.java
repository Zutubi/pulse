package com.cinnamonbob.core;

import com.cinnamonbob.core.model.BuildResult;

/**
 */
public class CommandCommencedEvent extends BuildEvent
{
    public CommandCommencedEvent(Object source, BuildResult result)
    {
        super(source, result);
    }
}
