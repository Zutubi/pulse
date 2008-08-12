package com.zutubi.pulse.events.build;

/**
 * This event is raised by the build controller when a recipe is completed
 * and artifact collection/cleanup and post-stage actions are about to run.
 */
public class RecipeCollectingEvent extends RecipeEvent
{
    public RecipeCollectingEvent(Object source, long recipeId)
    {
        super(source, recipeId);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Recipe Collection Completed Event");
        buff.append(": ").append(getRecipeId());
        return buff.toString();
    }
}
