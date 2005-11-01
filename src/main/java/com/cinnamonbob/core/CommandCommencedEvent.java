package com.cinnamonbob.core;

import com.cinnamonbob.model.BuildResult;

/**
 */
public class CommandCommencedEvent extends BuildEvent
{
    public CommandCommencedEvent(Object source, BuildResult result)
    {
        super(source, result);
    }
}
