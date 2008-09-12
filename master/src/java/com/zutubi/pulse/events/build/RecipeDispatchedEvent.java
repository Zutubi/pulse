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

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        RecipeDispatchedEvent event = (RecipeDispatchedEvent) o;

        if (!agent.equals(event.agent))
        {
            return false;
        }

        return request.equals(event.request);
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + request.hashCode();
        result = 31 * result + agent.hashCode();
        return result;
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Recipe Dispatched Event");
        buff.append(": ").append(getRecipeId());
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getName());
        }
        return buff.toString();
    }    
}
