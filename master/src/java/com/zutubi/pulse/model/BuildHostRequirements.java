package com.zutubi.pulse.model;

import com.zutubi.pulse.BuildService;

/**
 */
public interface BuildHostRequirements
{
    public BuildHostRequirements copy();

    public boolean fulfilledBy(BuildService service);

    public String getSummary();

}
