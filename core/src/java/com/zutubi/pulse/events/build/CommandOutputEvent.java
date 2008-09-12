package com.zutubi.pulse.events.build;

import java.util.Arrays;

/**
 * Carries a chunk of output from the currently-executing command.  These
 * events will only be sent if there is someone listening to output.
 */
public class CommandOutputEvent extends RecipeEvent
{
    private byte[] data;

    public CommandOutputEvent(Object source, long recipeId, byte[] data)
    {
        super(source, recipeId);
        this.data = data;
    }

    public byte[] getData()
    {
        return data;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Command Output Event: ");
        buff.append(getRecipeId());
        return buff.toString();
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

        CommandOutputEvent event = (CommandOutputEvent) o;
        return Arrays.equals(data, event.data);
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
