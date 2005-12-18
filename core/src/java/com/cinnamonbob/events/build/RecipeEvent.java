package com.cinnamonbob.events.build;

import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.model.RecipeResult;

/**
 */
public class RecipeEvent extends Event
{
    private RecipeResult result;

    public RecipeEvent(Object source, RecipeResult result)
    {
        super(source);
        this.result = result;
    }

    public RecipeResult getResult()
    {
        return result;
    }
}
