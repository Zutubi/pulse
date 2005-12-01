package com.cinnamonbob.core;

import com.cinnamonbob.core.model.RecipeResult;

/**
 */
public class CommandCommencedEvent extends RecipeEvent
{
    public CommandCommencedEvent(Object source, RecipeResult result)
    {
        super(source, result);
    }
}
