package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.events.RecipeEvent;

/**
 * This event is raised by the build controller when a recipe is completed
 * and artifact collection/cleanup and post-stage actions are about to run.
 *
 * @see RecipeCollectedEvent
 */
public class RecipeCollectingEvent extends RecipeEvent
{
    public RecipeCollectingEvent(Object source, long buildId, long recipeId)
    {
        super(source, buildId, recipeId);
    }

    public String toString()
    {
        return "Recipe Collecting Event" + ": " + getRecipeId();
    }
}
