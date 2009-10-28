package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceRequirement;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Simple implementation of {@link com.zutubi.pulse.core.ResourceRepository}
 * which maintains an in-memory cache of resources.
 */
public class InMemoryResourceRepository implements ResourceRepository
{
    private Map<String, Resource> resources = new HashMap<String, Resource>();

    public boolean hasResource(ResourceRequirement requirement)
    {
        Resource resource = getResource(requirement.getResource());
        return resource != null && hasRequiredVersion(resource, requirement);
    }

    private boolean hasRequiredVersion(Resource resource, ResourceRequirement requirement)
    {
        return requirement.isDefaultVersion() || resource.getVersion(requirement.getVersion()) != null;
    }

    public boolean hasResource(String name)
    {
        return getResource(name) != null;
    }

    public Resource getResource(String name)
    {
        return resources.get(name);
    }

    public List<String> getResourceNames()
    {
        return new LinkedList<String>(resources.keySet());
    }

    public void add(Resource resource)
    {
        resources.put(resource.getName(), resource);
    }
}
