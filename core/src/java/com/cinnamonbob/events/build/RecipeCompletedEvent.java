package com.cinnamonbob.events.build;

import com.cinnamonbob.core.model.RecipeResult;

/**
 * This event is raised by the recipe processor when a recipe is completed.
 */
public class RecipeCompletedEvent extends RecipeEvent
{
    public RecipeCompletedEvent(Object source, RecipeResult result)
    {
        super(source, result);
    }
}
