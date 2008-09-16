package com.zutubi.pulse.core.events;

/**
 */
public class CommandCommencedEvent extends RecipeEvent
{
    private String name;
    private long startTime;

    public CommandCommencedEvent(Object source, long recipeId, String name, long startTime)
    {
        super(source, recipeId);
        this.name = name;
        this.startTime = startTime;
    }

    public String getName()
    {
        return name;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Command Commenced Event: ");
        buff.append(getRecipeId());
        return buff.toString();
    }
}
