package com.cinnamonbob.core;

import com.cinnamonbob.core.model.RecipeResult;

/**
 * This event is raised by the recipe processor when commencing a recipe.
 */
public class RecipeCommencedEvent extends RecipeEvent
{
    public RecipeCommencedEvent(Object source, RecipeResult result)
    {
        super(source, result);
    }
}
