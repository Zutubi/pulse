package com.zutubi.pulse.events.build;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.core.events.RecipeEvent;

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
