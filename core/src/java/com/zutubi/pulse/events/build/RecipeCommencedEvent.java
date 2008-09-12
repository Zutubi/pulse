package com.zutubi.pulse.events.build;

/**
 * This event is raised by the recipe processor when commencing a recipe.
 */
public class RecipeCommencedEvent extends RecipeEvent
{
    private String name;
    private long startTime;

    public RecipeCommencedEvent(Object source, long recipeId, String name, long startTime)
    {
        super(source, recipeId);
        this.name = name;
        this.startTime = startTime;
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

        return !(name != null ? !name.equals(event.name) : event.name != null);
    }
    
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        return result;
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
        StringBuffer buff = new StringBuffer("Recipe Commenced Event");
        buff.append(": ").append(getRecipeId());
        return buff.toString();
    }    
}
