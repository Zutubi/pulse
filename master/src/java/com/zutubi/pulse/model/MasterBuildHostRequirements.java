package com.zutubi.pulse.model;

import com.zutubi.pulse.AgentService;
import com.zutubi.pulse.MasterAgentService;
import com.zutubi.pulse.RecipeDispatchRequest;

/**
 */
public class MasterBuildHostRequirements extends AbstractBuildHostRequirements
{
    public MasterBuildHostRequirements copy()
    {
        return new MasterBuildHostRequirements();
    }

    public boolean fulfilledBy(RecipeDispatchRequest request, AgentService service)
    {
        return service instanceof MasterAgentService;
    }

    public String getSummary()
    {
        return "master";
    }
}
