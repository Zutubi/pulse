package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.events.RecipeEvent;

/**
 */
public class RecipeTimeoutEvent extends RecipeEvent
{
    public RecipeTimeoutEvent(Object source, long buildId, long recipeId)
    {
        super(source, buildId, recipeId);
    }

    public String toString()
    {
        return "Recipe Timeout Event" + ": " + getRecipeId();
    }    
}
