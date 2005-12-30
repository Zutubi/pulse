package com.cinnamonbob.events.build;

import com.cinnamonbob.core.event.Event;

/**
 */
public class RecipeEvent extends Event
{
    private long recipeId;

    public RecipeEvent(Object source, long recipeId)
    {
        super(source);
        this.recipeId = recipeId;
    }

    public long getRecipeId()
    {
        return recipeId;
    }
}
