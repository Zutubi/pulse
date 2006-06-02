package com.zutubi.pulse.model;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.RecipeDispatchRequest;

import java.util.List;

/**
 */
public class AnyCapableBuildHostRequirements extends AbstractBuildHostRequirements
{
    public BuildHostRequirements copy()
    {
        return new AnyCapableBuildHostRequirements();
    }

    public boolean fulfilledBy(RecipeDispatchRequest request, BuildService service)
    {
        List<ResourceRequirement> requirements = request.getRequest().getResourceRequirements();
        for(ResourceRequirement requirement: requirements)
        {
            if(!service.hasResource(requirement.getResource(), requirement.getVersion()))
            {
                return false;
            }
        }
        return true;
    }

    public String getSummary()
    {
        return "[any]";
    }
}
