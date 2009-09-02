package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.ResourceRepositorySupport;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;

import java.util.Map;

/**
 * A resource repository backed by the configuration subsystem.
 */
public class AgentResourceRepository extends ResourceRepositorySupport
{
    private AgentConfiguration agentConfig;

    public AgentResourceRepository(AgentConfiguration agentConfig)
    {
        this.agentConfig = agentConfig;
    }

    public ResourceConfiguration getResource(String name)
    {
        return agentConfig.getResources().get(name);
    }

    public AgentConfiguration getAgentConfig()
    {
        return agentConfig;
    }

    public Map<String, ResourceConfiguration> getAll()
    {
        return agentConfig.getResources();
    }
}
