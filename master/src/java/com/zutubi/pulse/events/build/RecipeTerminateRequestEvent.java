package com.zutubi.pulse.events.build;

import com.zutubi.pulse.AgentService;
import com.zutubi.pulse.events.Event;

/**
 * Request to terminate a recipe, due to some error or a timeout.
 */
public class RecipeTerminateRequestEvent extends Event<Object>
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
        StringBuffer buff = new StringBuffer("Recipe Terminate Event");
        buff.append(": ").append(getRecipeId());
        return buff.toString();
    }    
}
