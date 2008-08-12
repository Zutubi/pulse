package com.zutubi.pulse.events.build;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.core.RecipeRequest;

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

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Recipe Dispatched Event");
        buff.append(": ").append(getRecipeId());
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getConfig().getName());
        }
        return buff.toString();
    }    
}
