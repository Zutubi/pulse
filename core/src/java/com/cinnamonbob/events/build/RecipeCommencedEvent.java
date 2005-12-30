package com.cinnamonbob.events.build;

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
}
