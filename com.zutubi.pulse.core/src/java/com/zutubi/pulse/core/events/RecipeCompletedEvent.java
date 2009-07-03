package com.zutubi.pulse.core.events;

import com.zutubi.pulse.core.model.RecipeResult;

/**
 * This event is raised by the recipe processor when a recipe is completed.
 */
public class RecipeCompletedEvent extends RecipeEvent
{
    private RecipeResult result;

    private RecipeCompletedEvent()
    {
        // For hessian
        super(null, 0);
    }

    public RecipeCompletedEvent(Object source, RecipeResult result)
    {
        super(source, result.getId());
        this.result = result;
    }

    public RecipeResult getResult()
    {
        return result;
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

        RecipeCompletedEvent event = (RecipeCompletedEvent) o;
        return result.equals(event.result);
    }

    public int hashCode()
    {
        int result1 = super.hashCode();
        result1 = 31 * result1 + result.hashCode();
        return result1;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Recipe Completed Event");
        buff.append(": ").append(getRecipeId());
        return buff.toString();
    }    
}
