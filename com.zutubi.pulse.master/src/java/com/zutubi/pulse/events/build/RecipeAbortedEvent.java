package com.zutubi.pulse.events.build;

import com.zutubi.pulse.core.events.RecipeEvent;

/**
 * This event is raised by the build controller when a recipe is aborted due
 * to abnormal build completion.
 */
public class RecipeAbortedEvent extends RecipeEvent
{
    public RecipeAbortedEvent(Object source, long recipeId)
    {
        super(source, recipeId);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Recipe Aborted Event");
        buff.append(": ").append(getRecipeId());
        return buff.toString();
    }
}
