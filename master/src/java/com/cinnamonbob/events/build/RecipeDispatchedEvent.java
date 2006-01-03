package com.cinnamonbob.events.build;

import com.cinnamonbob.BuildService;

/**
 * Raised when a recipe has been dispatched to a build host.
 */
public class RecipeDispatchedEvent extends RecipeEvent
{

    /**
     * The service the recipe was dispatched to.
     */
    private BuildService service;


    public RecipeDispatchedEvent(Object source, long recipeId, BuildService service)
    {
        super(source, recipeId);
        this.service = service;
    }

    public BuildService getService()
    {
        return service;
    }

}
