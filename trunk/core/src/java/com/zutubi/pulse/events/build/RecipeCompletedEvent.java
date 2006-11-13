package com.zutubi.pulse.events.build;

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

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Recipe Completed Event");
        buff.append(": ").append(getRecipeId());
        return buff.toString();
    }    
}
