package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.config.ResourceRequirement;

/**
 * A resource repository with no resources.
 */
public class EmptyResourceRepository implements ResourceRepository
{
    public boolean hasResource(ResourceRequirement requirement)
    {
        return false;
    }

    public boolean hasResource(String name)
    {
        return false;
    }

    public ResourceConfiguration getResource(String name)
    {
        return null;
    }
}
