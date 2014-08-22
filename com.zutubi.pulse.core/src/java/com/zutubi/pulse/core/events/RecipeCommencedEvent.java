package com.zutubi.pulse.core.events;

/**
 * This event is raised by the recipe processor when commencing a recipe.
 */
public class RecipeCommencedEvent extends RecipeEvent
{
    private String name;
    private String baseDir;
    private long startTime;

    public RecipeCommencedEvent(Object source, long buildId, long recipeId, String name, String baseDir, long startTime)
    {
        super(source, buildId, recipeId);
        this.name = name;
        this.baseDir = baseDir;
        this.startTime = startTime;
    }

    public String getName()
    {
        return name;
    }

    public String getBaseDir()
    {
        return baseDir;
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

        RecipeCommencedEvent event = (RecipeCommencedEvent) o;
        if (startTime != event.startTime)
        {
            return false;
        }

        if ((baseDir != null ? !baseDir.equals(event.baseDir) : event.baseDir != null))
        {
            return false;
        }

        return !(name != null ? !name.equals(event.name) : event.name != null);
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (baseDir != null ? baseDir.hashCode() : 0);
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        return result;
    }

    public String toString()
    {
        return "Recipe Commenced Event" + ": " + getRecipeId();
    }    
}
