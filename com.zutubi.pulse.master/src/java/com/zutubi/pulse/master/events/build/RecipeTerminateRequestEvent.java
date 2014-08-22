package com.zutubi.pulse.master.events.build;

import com.zutubi.events.Event;
import com.zutubi.pulse.master.agent.AgentService;

/**
 * Request to terminate a recipe, due to some error or a timeout.
 */
public class RecipeTerminateRequestEvent extends Event
{
    private AgentService service;
    private long recipeId;

    public RecipeTerminateRequestEvent(Object source, AgentService service, long recipeId)
    {
        super(source);
        this.service = service;
        this.recipeId = recipeId;
    }

    public AgentService getService()
    {
        return service;
    }

    public long getRecipeId()
    {
        return recipeId;
    }

    public String toString()
    {
        return "Recipe Terminate Event" + ": " + getRecipeId();
    }    
}
