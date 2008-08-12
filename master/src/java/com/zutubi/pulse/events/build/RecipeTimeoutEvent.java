package com.zutubi.pulse.events.build;

/**
 */
public class RecipeTimeoutEvent extends RecipeEvent
{
    private long buildId;

    public RecipeTimeoutEvent(Object source, long buildId, long recipeId)
    {
        super(source, recipeId);
        this.buildId = buildId;
    }

    public long getBuildId()
    {
        return buildId;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Recipe Timeout Event");
        buff.append(": ").append(getRecipeId());
        return buff.toString();
    }    
}
