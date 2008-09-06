package com.zutubi.pulse.events.build;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.core.RecipeRequest;

/**
 * Raised when a recipe has been assigned to an agent.
 */
public class RecipeAssignedEvent extends RecipeEvent
{
    /**
     * The request that has been assigned.
     */
    RecipeRequest request;
    /**
     * The agent the recipe was assigned to.
     */
    private Agent agent;

    public RecipeAssignedEvent(Object source, RecipeRequest request, Agent agent)
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
        StringBuffer buff = new StringBuffer("Recipe Assigned Event");
        buff.append(": ").append(getRecipeId());
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getConfig().getName());
        }
        return buff.toString();
    }    
}
