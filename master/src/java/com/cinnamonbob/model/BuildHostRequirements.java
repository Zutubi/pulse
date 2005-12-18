package com.cinnamonbob.model;

import com.cinnamonbob.BuildService;

/**
 */
public interface BuildHostRequirements
{
    public boolean fulfilledBy(BuildService service);
}
