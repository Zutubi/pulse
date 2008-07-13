package com.zutubi.pulse.model;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.tove.config.agent.AgentConfiguration;

import java.util.List;
import java.util.Map;

/**
 */
public interface ResourceManager
{
    ResourceRepository getAgentRepository(long handle);

    ResourceRepository getAgentRepository(AgentConfiguration agent);

    ResourceRepository getAgentRepository(Agent agent);

    void addDiscoveredResources(String agentPath, List<Resource> resources);

    Map<String, List<Resource>> findAll();
}
