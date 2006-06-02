package com.zutubi.pulse.model;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.RecipeDispatchRequest;

/**
 */
public interface BuildHostRequirements
{
    public BuildHostRequirements copy();

    public boolean fulfilledBy(RecipeDispatchRequest request, BuildService service);

    public String getSummary();

}
