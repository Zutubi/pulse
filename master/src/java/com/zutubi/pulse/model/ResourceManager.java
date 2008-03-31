package com.zutubi.pulse.model;

import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.config.Resource;

import java.util.List;
import java.util.Map;

/**
 */
public interface ResourceManager
{
    ResourceRepository getAgentRepository(long handle);

    void addDiscoveredResources(String agentPath, List<Resource> resources);

    Map<String, List<Resource>> findAll();
}
