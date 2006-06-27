package com.zutubi.pulse.model;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.MasterBuildService;

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

    public String getHostName()
    {
        return "[master]";
    }

    public void setService(MasterBuildService service)
    {
        this.service = service;
    }
}
