package com.cinnamonbob.events.build;

import com.cinnamonbob.BuildService;
import com.cinnamonbob.core.event.Event;

/**
 * Raised when a recipe has been dispatched to a build host.
 */
public class RecipeDispatchedEvent extends Event
{
    /**
     * THe id of the recipe that was dispatched
     */
    private long recipeId;
    /**
     * The service the recipe was dispatched to.
     */
    private BuildService service;


    public RecipeDispatchedEvent(Object source, long recipeId, BuildService service)
    {
        super(source);
        // TODO is this a RecipeEvent (once we remove the whole result from there)?
        this.recipeId = recipeId;
        this.service = service;
    }

    public BuildService getService()
    {
        return service;
    }

    public long getRecipeId()
    {
        return recipeId;
    }
}
