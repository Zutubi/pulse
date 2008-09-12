package com.zutubi.pulse.events.build;

import com.zutubi.pulse.core.model.CommandResult;

/**
 */
public class CommandCompletedEvent extends RecipeEvent
{
    private CommandResult result;

    public CommandCompletedEvent(Object source, long recipeId, CommandResult result)
    {
        super(source, recipeId);
        this.result = result;
    }

    public CommandResult getResult()
    {
        return result;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        CommandCompletedEvent event = (CommandCompletedEvent) o;
        return result.equals(event.result);
    }

    public int hashCode()
    {
        int result1 = super.hashCode();
        result1 = 31 * result1 + result.hashCode();
        return result1;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Command Completed Event: ");
        buff.append(getRecipeId());
        return buff.toString();
    }
}
