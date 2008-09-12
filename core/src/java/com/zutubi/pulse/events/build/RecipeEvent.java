package com.zutubi.pulse.events.build;

import com.zutubi.pulse.events.Event;

/**
 */
public class RecipeEvent extends Event<Object>
{
    private long recipeId;

    public RecipeEvent(Object source, long recipeId)
    {
        super(source);
        this.recipeId = recipeId;
    }

    public long getRecipeId()
    {
        return recipeId;
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

        RecipeEvent event = (RecipeEvent) o;
        return recipeId == event.recipeId;
    }

    public int hashCode()
    {
        return (int) (recipeId ^ (recipeId >>> 32));
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Recipe Event");
        buff.append(": ").append(getRecipeId());
        return buff.toString();
    }    
}
