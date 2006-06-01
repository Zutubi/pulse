package com.zutubi.pulse.events.build;

import com.zutubi.pulse.events.Event;

/**
 */
public class RecipeEvent extends Event<Object>
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
