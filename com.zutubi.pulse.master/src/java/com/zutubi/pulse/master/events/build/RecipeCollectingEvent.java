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
    public RecipeCollectingEvent(Object source, long recipeId)
    {
        super(source, recipeId);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Recipe Collecting Event");
        buff.append(": ").append(getRecipeId());
        return buff.toString();
    }
}
