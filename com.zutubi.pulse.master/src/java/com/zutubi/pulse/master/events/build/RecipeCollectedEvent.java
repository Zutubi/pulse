package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.events.RecipeEvent;

/**
 * This event is raised by the build controller when a recipe is completed
 * and the artifacts have been collected/cleaned up on the slave.  Note this
 * is also after the post stage actions have run.
 */
public class RecipeCollectedEvent extends RecipeEvent
{
    public RecipeCollectedEvent(Object source, long recipeId)
    {
        super(source, recipeId);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Recipe Collected Event");
        buff.append(": ").append(getRecipeId());
        return buff.toString();
    }
}
