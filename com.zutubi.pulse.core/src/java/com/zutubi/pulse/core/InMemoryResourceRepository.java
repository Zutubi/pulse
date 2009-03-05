package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.config.ResourceRequirement;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Here be resources.  Yar.
 */
public class InMemoryResourceRepository implements ResourceRepository
{
    private Map<String, ResourceConfiguration> resources = new TreeMap<String, ResourceConfiguration>();

    public void addResource(ResourceConfiguration r)
    {
        resources.put(r.getName(), r);
    }

    public boolean hasResource(ResourceRequirement requirement)
    {
        String name = requirement.getResource();
        String version = requirement.getVersion();

        ResourceConfiguration r = getResource(name);
        if (r == null)
        {
            return false;
        }

        return requirement.isDefaultVersion() || r.getVersion(version) != null;
    }

    public boolean hasResource(String name)
    {
        return resources.containsKey(name);
    }

    public ResourceConfiguration getResource(String name)
    {
        return resources.get(name);
    }

    public List<String> getResourceNames()
    {
        return new LinkedList<String>(resources.keySet());
    }
}
