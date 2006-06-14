package com.zutubi.pulse.model;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.MasterBuildService;
import com.zutubi.pulse.RecipeDispatchRequest;

/**
 */
public class MasterBuildHostRequirements extends AbstractBuildHostRequirements
{
    public MasterBuildHostRequirements copy()
    {
        return new MasterBuildHostRequirements();
    }

    public boolean fulfilledBy(RecipeDispatchRequest request, BuildService service)
    {
        return service instanceof MasterBuildService;
    }

    public String getSummary()
    {
        return "[master]";
    }
}
