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
