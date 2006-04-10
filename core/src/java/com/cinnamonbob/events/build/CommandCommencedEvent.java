package com.zutubi.pulse.events.build;

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
}
