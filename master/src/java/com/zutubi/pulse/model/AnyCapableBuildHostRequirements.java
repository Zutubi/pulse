package com.zutubi.pulse.model;

import com.zutubi.pulse.BuildService;

/**
 */
public class AnyCapableBuildHostRequirements extends AbstractBuildHostRequirements
{
    public BuildHostRequirements copy()
    {
        return new AnyCapableBuildHostRequirements();
    }

    public boolean fulfilledBy(BuildService service)
    {
        // TODO: dev-distributed
        return true;
    }

    public String getSummary()
    {
        return "[any]";
    }
}
