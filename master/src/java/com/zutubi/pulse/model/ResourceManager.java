package com.zutubi.pulse.model;

import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.ResourceRepository;

import java.util.List;
import java.util.Map;

/**
 */
public interface ResourceManager
{
    ResourceRepository getAgentRepository(long handle);

    void addDiscoveredResources(long handle, List<Resource> resources);

    Map<String, List<Resource>> findAll();
}
