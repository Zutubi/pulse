package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceConfiguration;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A repository that holds a cache of resource information in memory.
 */
public class InMemoryResourceRepository extends ResourceRepositorySupport
{
    private Map<String, ResourceConfiguration> resources = new TreeMap<String, ResourceConfiguration>();

    public ResourceConfiguration getResource(String name)
    {
        return resources.get(name);
    }

    /**
     * Add the given resource to this repository.  If a resource of the same
     * name already exists, this one replaces it.
     *
     * @param resource the resource to add
     */
    public void addResource(ResourceConfiguration resource)
    {
        resources.put(resource.getName(), resource);
    }

    /**
     * Returns the names of all resources in this repository.
     *
     * @return names for all if the resources in this repository
     */
    public List<String> getResourceNames()
    {
        return new LinkedList<String>(resources.keySet());
    }
}
