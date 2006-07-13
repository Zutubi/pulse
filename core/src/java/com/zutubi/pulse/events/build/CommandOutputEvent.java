package com.zutubi.pulse.events.build;

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
}
