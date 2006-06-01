package com.zutubi.pulse.model;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.MasterBuildService;

/**
 */
public class MasterBuildHostRequirements extends AbstractBuildHostRequirements
{
    public MasterBuildHostRequirements copy()
    {
        return new MasterBuildHostRequirements();
    }

    public boolean fulfilledBy(BuildService service)
    {
        return service instanceof MasterBuildService;
    }

    public String getSummary()
    {
        return "[master]";
    }
}
