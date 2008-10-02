package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.events.RecipeEvent;

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

        RecipeTimeoutEvent event = (RecipeTimeoutEvent) o;
        return buildId == event.buildId;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (int) (buildId ^ (buildId >>> 32));
        return result;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Recipe Timeout Event");
        buff.append(": ").append(getRecipeId());
        return buff.toString();
    }    
}
