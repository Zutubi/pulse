package com.zutubi.pulse.core.events;

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

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Recipe Event");
        buff.append(": ").append(getRecipeId());
        return buff.toString();
    }    
}
