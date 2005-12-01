package com.cinnamonbob.core;

import com.cinnamonbob.core.model.RecipeResult;

/**
 */
public class CommandCompletedEvent extends RecipeEvent
{
    public CommandCompletedEvent(Object source, RecipeResult result)
    {
        super(source, result);
    }
}
