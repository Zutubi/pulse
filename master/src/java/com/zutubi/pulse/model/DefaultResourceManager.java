package com.zutubi.pulse.model;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.TypeListener;
import com.zutubi.pulse.core.ConfigurableResourceRepository;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;

import java.util.*;

/**
 */
public class DefaultResourceManager implements ResourceManager
{
    private Map<Long, ConfigurationResourceRepository> agentRepositories = new TreeMap<Long, ConfigurationResourceRepository>();
    private ConfigurationProvider configurationProvider;
    private Map<Long, Resource> resourcesByHandle = new HashMap<Long, Resource>();
    private Map<Long, ResourceVersion> resourceVersionsByHandle = new HashMap<Long, ResourceVersion>();

    public void init()
    {
        TypeListener<AgentConfiguration> agentListener = new TypeListener<AgentConfiguration>(AgentConfiguration.class)
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
        agentListener.register(configurationProvider);

        TypeListener<Resource> resourceListener = new TypeListener<Resource>(Resource.class)
        {
            public void postInsert(Resource instance)
            {
                addResource(instance);
            }

            public void postDelete(Resource instance)
            {
                removeResource(instance);
            }

            public void postSave(Resource instance)
            {
                updateResource(instance);
            }
        };
        resourceListener.register(configurationProvider);

        TypeListener<ResourceVersion> resourceVersionListener = new TypeListener<ResourceVersion>(ResourceVersion.class)
        {
            public void postInsert(ResourceVersion instance)
            {
                addResourceVersion(instance);
            }

            public void postDelete(ResourceVersion instance)
            {
                removeResourceVersion(instance);
            }

            public void postSave(ResourceVersion instance)
            {
                updateResourceVersion(instance);
            }
        };
        resourceVersionListener.register(configurationProvider);

        for (Resource resource : configurationProvider.getAll(Resource.class))
        {
            addResource(resource);
        }

        for (ResourceVersion resourceVersion : configurationProvider.getAll(ResourceVersion.class))
        {
            addResourceVersion(resourceVersion);
        }

        for (AgentConfiguration agentConfig : configurationProvider.getAll(AgentConfiguration.class))
        {
            addAgentRepo(agentConfig);
        }
    }

    private void addResource(Resource resource)
    {
        resourcesByHandle.put(resource.getHandle(), resource);
    }

    private void removeResource(Resource resource)
    {
        resourcesByHandle.remove(resource.getHandle());
    }

    private void updateResource(Resource resource)
    {
        Resource oldResource = resourcesByHandle.remove(resource.getHandle());
        if (oldResource != null)
        {
            String oldName = oldResource.getName();
            String newName = resource.getName();
            if (!oldName.equals(newName))
            {
                for (ResourceRequirement requirement : configurationProvider.getAll(ResourceRequirement.class))
                {
                    if (requirement.getResource().equals(oldName))
                    {
                        requirement.setResource(newName);
                        configurationProvider.save(requirement);
                    }
                }
            }

            addResource(resource);
        }
    }

    private void addResourceVersion(ResourceVersion resourceVersion)
    {
        resourceVersionsByHandle.put(resourceVersion.getHandle(), resourceVersion);
    }

    private void removeResourceVersion(ResourceVersion resourceVersion)
    {
        resourceVersionsByHandle.remove(resourceVersion.getHandle());
    }

    private void updateResourceVersion(ResourceVersion resourceVersion)
    {
        ResourceVersion oldVersion = resourceVersionsByHandle.remove(resourceVersion.getHandle());
        if (oldVersion != null)
        {
            String oldValue = oldVersion.getValue();
            String newValue = resourceVersion.getValue();
            if (!oldValue.equals(newValue))
            {
                Resource owningResource = configurationProvider.getAncestorOfType(resourceVersion, Resource.class);
                String resourceName = owningResource.getName();
                for (ResourceRequirement requirement : configurationProvider.getAll(ResourceRequirement.class))
                {
                    if (requirement.getResource().equals(resourceName) && requirement.getVersion().equals(oldValue))
                    {
                        requirement.setVersion(newValue);
                        configurationProvider.save(requirement);
                    }
                }
            }

            addResourceVersion(resourceVersion);
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
            for (Resource r : resources)
            {
                if (!repository.hasResource(r.getName()))
                {
                    repository.addResource(r);
                }
            }
        }
    }

    public Map<String, List<Resource>> findAll()
    {
        Map<String, List<Resource>> allResources = new HashMap<String, List<Resource>>();
        for (ConfigurationResourceRepository repo : agentRepositories.values())
        {
            allResources.put(repo.getAgentConfig().getName(), new LinkedList<Resource>(repo.getAll().values()));
        }

        return allResources;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
