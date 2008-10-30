package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.config.Resource;

import java.util.List;

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

    public Resource getResource(String name)
    {
        return null;
    }

    public List<String> getResourceNames()
    {
        return null;
    }
}
