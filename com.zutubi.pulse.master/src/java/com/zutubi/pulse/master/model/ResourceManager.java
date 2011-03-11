package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public interface ResourceManager
{
    ResourceRepository getAgentRepository(long handle);

    ResourceRepository getAgentRepository(AgentConfiguration agent);

    ResourceRepository getAgentRepository(Agent agent);

    /**
     * Returns the set of agents that are capable of running a recipe with the
     * given resource requirements.  All non-optional requirements must be
     * present in the agent's resource repository for it to be capable.
     *
     * @param requirements the requirements to test for
     * @return the set of all agents that can satisfy the requirements
     */
    Set<AgentConfiguration> getCapableAgents(Collection<? extends ResourceRequirement> requirements);

    List<AgentConfiguration> addDiscoveredResources(String location, List<ResourceConfiguration> resources);

    Map<String, List<ResourceConfiguration>> findAll();
}
