package com.cinnamonbob.model;

import com.cinnamonbob.BuildService;
import com.cinnamonbob.MasterBuildService;

/**
 * <class-comment/>
 */
public class MasterBuildServiceResolver extends AbstractBuildServiceResolver
{
    private MasterBuildService service;

    public BuildService resolve()
    {
        return service;
    }

    public void setService(MasterBuildService service)
    {
        this.service = service;
    }
}
