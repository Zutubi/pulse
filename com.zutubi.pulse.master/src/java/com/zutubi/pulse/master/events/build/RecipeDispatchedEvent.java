package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.events.RecipeEvent;
import com.zutubi.pulse.master.agent.Agent;

/**
 * Raised when a recipe has been dispatched to an agent.
 */
public class RecipeDispatchedEvent extends RecipeEvent
{
    /**
     * The agent the recipe was dispatched to.
     */
    private Agent agent;

    /**
     * @param source   source of the event
     * @param recipeId id of the recipe that has been dispatched
     * @param agent    agent the recipe was dispatched to
     */
    public RecipeDispatchedEvent(Object source, long recipeId, Agent agent)
    {
        super(source, recipeId);
        this.agent = agent;
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
        return agent.equals(event.agent);
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + agent.hashCode();
        return result;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder("Recipe Dispatched Event");
        builder.append(": ").append(getRecipeId());
        if (getAgent() != null)
        {
            builder.append(": ").append(getAgent().getConfig().getName());
        }
        return builder.toString();
    }
}
