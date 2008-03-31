package com.zutubi.pulse.model;

import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;
import com.zutubi.util.TextUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A resource repository backed by the configuration subsystem.
 */
public class ConfigurationResourceRepository implements ResourceRepository
{
    private AgentConfiguration agentConfig;

    public ConfigurationResourceRepository(AgentConfiguration agentConfig)
    {
        this.agentConfig = agentConfig;
    }

    public AgentConfiguration getAgentConfig()
    {
        return agentConfig;
    }

    public boolean hasResource(String name, String version)
    {
        Resource r = getResource(name);
        return r != null && (!TextUtils.stringSet(version) || r.hasVersion(version));
    }

    public boolean hasResource(String name)
    {
        return getResource(name) != null;
    }

    public Resource getResource(String name)
    {
        return agentConfig.getResources().get(name);
    }

    public List<String> getResourceNames()
    {
        return new LinkedList<String>(agentConfig.getResources().keySet());
    }

    public Map<String, Resource> getAll()
    {
        return agentConfig.getResources();
    }
}
