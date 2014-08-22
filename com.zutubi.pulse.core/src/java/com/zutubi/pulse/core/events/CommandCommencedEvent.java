package com.zutubi.pulse.core.events;

/**
 * Event raised when a command is started.
 */
public class CommandCommencedEvent extends RecipeEvent
{
    private String name;
    private long startTime;

    public CommandCommencedEvent(Object source, long buildId, long recipeId, String name, long startTime)
    {
        super(source, buildId, recipeId);
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

        CommandCommencedEvent event = (CommandCommencedEvent) o;

        if (startTime != event.startTime)
        {
            return false;
        }
        return !(name != null ? !name.equals(event.name) : event.name != null);
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        return result;
    }

    public String toString()
    {
        return "Command Commenced Event: " + getRecipeId();
    }
}
