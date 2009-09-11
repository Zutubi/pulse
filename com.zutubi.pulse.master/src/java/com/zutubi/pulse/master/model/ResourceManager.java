package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;

import java.util.List;
import java.util.Map;

/**
 */
public interface ResourceManager
{
    ResourceRepository getAgentRepository(long handle);

    ResourceRepository getAgentRepository(AgentConfiguration agent);

    ResourceRepository getAgentRepository(Agent agent);

    List<AgentConfiguration> addDiscoveredResources(String location, List<ResourceConfiguration> resources);

    Map<String, List<ResourceConfiguration>> findAll();
}
