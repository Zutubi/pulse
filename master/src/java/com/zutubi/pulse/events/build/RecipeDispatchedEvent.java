package com.zutubi.pulse.events.build;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.RecipeRequest;
import com.zutubi.pulse.agent.Agent;

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
     * The agent the recipe was dispatched to.
     */
    private Agent agent;


    public RecipeDispatchedEvent(Object source, RecipeRequest request, Agent agent)
    {
        super(source, request.getId());
        this.request = request;
        this.agent = agent;
    }

    public RecipeRequest getRequest()
    {
        return request;
    }

    public Agent getAgent()
    {
        return agent;
    }
}
