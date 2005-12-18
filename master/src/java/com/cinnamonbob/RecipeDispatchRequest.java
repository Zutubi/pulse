package com.cinnamonbob;

import com.cinnamonbob.model.BuildHostRequirements;

/**
 * A request to dispatch a recipe to some build hostRequirements, which may be restricted.
 */
public class RecipeDispatchRequest
{
    private BuildHostRequirements hostRequirements;
    private RecipeRequest request;

    public RecipeDispatchRequest(BuildHostRequirements hostRequirements, RecipeRequest request)
    {
        this.hostRequirements = hostRequirements;
        this.request = request;
    }

    public BuildHostRequirements getHostRequirements()
    {
        return hostRequirements;
    }

    public RecipeRequest getRequest()
    {
        return request;
    }
}
