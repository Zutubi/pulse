package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.events.RecipeEvent;
import com.zutubi.pulse.master.agent.Agent;

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
        super(source, request.getBuildId(), request.getId());
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

        RecipeAssignedEvent event = (RecipeAssignedEvent) o;
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
        StringBuffer buff = new StringBuffer("Recipe Assigned Event");
        buff.append(": ").append(getRecipeId());
        if (getAgent() != null)
        {
            buff.append(": ").append(getAgent().getConfig().getName());
        }
        return buff.toString();
    }    
}
