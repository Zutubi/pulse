package com.zutubi.pulse.core;

import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;

import java.util.Collection;

/**
 * A resource repository with no resources.
 */
public class EmptyResourceRepository implements ResourceRepository
{
    public boolean hasResource(ResourceRequirement requirement)
    {
        return false;
    }

    public boolean satisfies(Iterable<? extends ResourceRequirement> requirements)
    {
        for (ResourceRequirement requirement: requirements)
        {
            if (!requirement.isOptional())
            {
                return false;
            }
        }

        return true;
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
