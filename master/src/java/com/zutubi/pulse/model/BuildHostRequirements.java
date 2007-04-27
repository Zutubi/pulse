package com.zutubi.pulse.model;

import com.zutubi.pulse.AgentService;
import com.zutubi.pulse.RecipeDispatchRequest;

/**
 */
public interface BuildHostRequirements
{
    public BuildHostRequirements copy();

    public boolean fulfilledBy(RecipeDispatchRequest request, AgentService service);

    public String getSummary();

}
