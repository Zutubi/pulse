package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration type used specifically for loading resources from a file.
 */
@SymbolicName("zutubi.resourcesConfig")
public class ResourcesConfiguration extends AbstractConfiguration
{
    private Map<String, ResourceConfiguration> resources = new HashMap<String, ResourceConfiguration>();

    public Map<String, ResourceConfiguration> getResources()
    {
        return resources;
    }

    public void setResources(Map<String, ResourceConfiguration> resources)
    {
        this.resources = resources;
    }
}
