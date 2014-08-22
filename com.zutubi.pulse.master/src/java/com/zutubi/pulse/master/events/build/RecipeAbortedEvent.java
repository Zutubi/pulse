package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.events.RecipeEvent;

/**
 * This event is raised by the build controller when a recipe is aborted due
 * to abnormal build completion.
 */
public class RecipeAbortedEvent extends RecipeEvent
{
    public RecipeAbortedEvent(Object source, long buildId, long recipeId)
    {
        super(source, buildId, recipeId);
    }

    public String toString()
    {
        return "Recipe Aborted Event" + ": " + getRecipeId();
    }
}
