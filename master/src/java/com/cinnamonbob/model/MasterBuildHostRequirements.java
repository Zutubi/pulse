package com.cinnamonbob.model;

import com.cinnamonbob.BuildService;
import com.cinnamonbob.MasterBuildService;

/**
 */
public class MasterBuildHostRequirements extends AbstractBuildHostRequirements
{

    public boolean fulfilledBy(BuildService service)
    {
        return service instanceof MasterBuildService;
    }

    public String getSummary()
    {
        return "[master]";
    }
}
