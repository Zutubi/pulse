package com.zutubi.pulse.core.events;

/**
 *
 */
public class CommandSkippedEvent extends RecipeEvent
{
    public CommandSkippedEvent(Object source, long recipeId)
    {
        super(source, recipeId);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Command Skipped Event: ");
        buff.append(getRecipeId());
        return buff.toString();
    }
}
