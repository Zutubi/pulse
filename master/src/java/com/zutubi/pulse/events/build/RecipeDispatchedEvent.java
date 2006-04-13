/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.events.build;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.RecipeRequest;

/**
 * Raised when a recipe has been dispatched to a build host.
 */
public class RecipeDispatchedEvent extends RecipeEvent
{
    /**
     * The request that was dispatched.
     */
    RecipeRequest request;
    /**
     * The service the recipe was dispatched to.
     */
    private BuildService service;


    public RecipeDispatchedEvent(Object source, RecipeRequest request, BuildService service)
    {
        super(source, request.getId());
        this.request = request;
        this.service = service;
    }

    public RecipeRequest getRequest()
    {
        return request;
    }

    public BuildService getService()
    {
        return service;
    }

}
