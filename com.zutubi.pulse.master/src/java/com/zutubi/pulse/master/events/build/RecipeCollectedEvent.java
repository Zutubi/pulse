package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.events.RecipeEvent;

/**
 * This event is raised by the build controller when a recipe is completed
 * and the artifacts have been collected/cleaned up on the slave.  Note this
 * is also after the post stage actions have run.
 */
public class RecipeCollectedEvent extends RecipeEvent
{
    public RecipeCollectedEvent(Object source, long buildId, long recipeId)
    {
        super(source, buildId, recipeId);
    }

    public String toString()
    {
        return "Recipe Collected Event" + ": " + getRecipeId();
    }
}
