package com.zutubi.pulse.events.build;

import com.zutubi.pulse.events.Event;

/**
 */
public class RecipeTimeoutEvent extends Event<Object>
{
    private long buildId;
    private long recipeId;

    public RecipeTimeoutEvent(Object source, long buildId, long recipeId)
    {
        super(source);
        this.buildId = buildId;
        this.recipeId = recipeId;
    }

    public long getBuildId()
    {
        return buildId;
    }

    public long getRecipeId()
    {
        return recipeId;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Build Recipe Timeout Event");
        buff.append(": ").append(getRecipeId());
        return buff.toString();
    }    
}
