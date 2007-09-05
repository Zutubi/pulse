package com.zutubi.pulse.model;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.TypeListener;
import com.zutubi.pulse.core.ConfigurableResourceRepository;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class DefaultResourceManager implements ResourceManager
{
    private Map<Long, ConfigurationResourceRepository> agentRepositories = new TreeMap<Long, ConfigurationResourceRepository>();
    private ConfigurationProvider configurationProvider;

    public void init()
    {
        TypeListener<AgentConfiguration> listener = new TypeListener<AgentConfiguration>(AgentConfiguration.class)
        {
            public void postInsert(AgentConfiguration instance)
            {
                addAgentRepo(instance);
            }

            public void postDelete(AgentConfiguration instance)
            {
                agentRepositories.remove(instance.getHandle());
            }

            public void postSave(AgentConfiguration instance)
            {
                // Replaces the existing as it is stored by the (unchanging)
                // handle.
                addAgentRepo(instance);
            }
        };
        listener.register(configurationProvider);

        for(AgentConfiguration agentConfig: configurationProvider.getAll(AgentConfiguration.class))
        {
            addAgentRepo(agentConfig);
        }
    }

    private void addAgentRepo(AgentConfiguration agentConfiguration)
    {
        agentRepositories.put(agentConfiguration.getHandle(), new ConfigurationResourceRepository(agentConfiguration, configurationProvider));
    }

    public ResourceRepository getAgentRepository(long handle)
    {
        return agentRepositories.get(handle);
    }

    public void addDiscoveredResources(long handle, List<Resource> resources)
    {
        ConfigurableResourceRepository repository = agentRepositories.get(handle);
        if (repository != null)
        {
            for(Resource r: resources)
            {
                if(!repository.hasResource(r.getName()))
                {
                    repository.addResource(r);
                }
            }
        }
    }

    public Map<String, Resource> findAll()
    {
        Map<String, Resource> allResources = new HashMap<String, Resource>();
        for(ConfigurationResourceRepository repo: agentRepositories.values())
        {
            allResources.putAll(repo.getAll());
        }

        return allResources;
    }

    public void editResource(PersistentResource resource, String newName, String defaultVersion)
    {
        // FIXME remember to catch config events to do this
//        List<BuildSpecificationNode> nodes = buildSpecificationNodeDao.findByResourceRequirement(resource.getName());
//        for(BuildSpecificationNode node: nodes)
//        {
//            for(ResourceRequirement r: node.getResourceRequirements())
//            {
//                if(r.getResource().equals(resource.getName()))
//                {
//                    r.setResource(newName);
//                }
//            }
//
//            buildSpecificationNodeDao.save(node);
//        }

        resource.setName(newName);
        resource.setDefaultVersion(defaultVersion);
    }

    public void renameResourceVersion(PersistentResource resource, String value, String newValue)
    {
        // FIXME remember to catch config events to do this
//        List<BuildSpecificationNode> nodes = buildSpecificationNodeDao.findByResourceRequirement(resource.getName());
//        for(BuildSpecificationNode node: nodes)
//        {
//            for(ResourceRequirement r: node.getResourceRequirements())
//            {
//                if(r.getResource().equals(resource.getName()) && value.equals(r.getVersion()))
//                {
//                    r.setVersion(newValue);
//                }
//            }
//
//            buildSpecificationNodeDao.save(node);
//        }

        ResourceVersion version = resource.getVersion(value);
        resource.deleteVersion(version);
        version.setValue(newValue);
        resource.add(version);
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
