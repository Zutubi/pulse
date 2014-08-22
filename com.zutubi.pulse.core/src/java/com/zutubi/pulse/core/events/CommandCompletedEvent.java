package com.zutubi.pulse.core.events;

import com.zutubi.pulse.core.model.CommandResult;

/**
 */
public class CommandCompletedEvent extends RecipeEvent
{
    private CommandResult result;

    public CommandCompletedEvent(Object source, long buildId, long recipeId, CommandResult result)
    {
        super(source, buildId, recipeId);
        this.result = result;
    }

    public CommandResult getResult()
    {
        return result;
    }

    public String toString()
    {
        return "Command Completed Event: " + getRecipeId();
    }
}
