package com.zutubi.pulse.model;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.TypeAdapter;
import com.zutubi.prototype.config.TypeListener;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.events.system.ConfigurationSystemStartedEvent;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.logging.Logger;

import java.util.*;

/**
 */
public class DefaultResourceManager implements ResourceManager, com.zutubi.pulse.events.EventListener
{
    private static final Logger LOG = Logger.getLogger(DefaultResourceManager.class);

    private Map<Long, ConfigurationResourceRepository> agentRepositories = new TreeMap<Long, ConfigurationResourceRepository>();
    private ConfigurationProvider configurationProvider;
    private Map<Long, Resource> resourcesByHandle = new HashMap<Long, Resource>();
    private Map<Long, ResourceVersion> resourceVersionsByHandle = new HashMap<Long, ResourceVersion>();

    public void init(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
        TypeListener<AgentConfiguration> agentListener = new TypeAdapter<AgentConfiguration>(AgentConfiguration.class)
        {
            public void postInsert(AgentConfiguration instance)
            {
                addAgentRepo(instance);
            }

            public void postDelete(AgentConfiguration instance)
            {
                agentRepositories.remove(instance.getHandle());
            }

            public void postSave(AgentConfiguration instance, boolean nested)
            {
                // Replaces the existing as it is stored by the (unchanging)
                // handle.
                addAgentRepo(instance);
            }
        };
        agentListener.register(configurationProvider, true);

        TypeListener<Resource> resourceListener = new TypeAdapter<Resource>(Resource.class)
        {
            public void save(Resource instance, boolean nested)
            {
                if (!nested)
                {
                    updateResource(instance);
                }
            }

            public void postInsert(Resource instance)
            {
                addResource(instance);
            }

            public void postDelete(Resource instance)
            {
                removeResource(instance);
            }

            public void postSave(Resource instance, boolean nested)
            {
                // Replaces the existing as it is stored by the (unchanging)
                // handle.
                addResource(instance);
            }
        };
        resourceListener.register(configurationProvider, true);

        TypeListener<ResourceVersion> resourceVersionListener = new TypeAdapter<ResourceVersion>(ResourceVersion.class)
        {
            public void save(ResourceVersion instance, boolean nested)
            {
                if (!nested)
                {
                    updateResourceVersion(instance);
                }
            }

            public void postInsert(ResourceVersion instance)
            {
                addResourceVersion(instance);
            }

            public void postDelete(ResourceVersion instance)
            {
                removeResourceVersion(instance);
            }

            public void postSave(ResourceVersion instance, boolean nested)
            {
                // Replaces the existing as it is stored by the (unchanging)
                // handle.
                addResourceVersion(instance);
            }
        };
        resourceVersionListener.register(configurationProvider, true);

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
        Resource oldResource = resourcesByHandle.get(resource.getHandle());
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
                        ResourceRequirement clone = configurationProvider.deepClone(requirement);
                        clone.setResource(newName);
                        configurationProvider.save(clone);
                    }
                }
            }
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
        ResourceVersion oldVersion = resourceVersionsByHandle.get(resourceVersion.getHandle());
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
                        ResourceRequirement clone = configurationProvider.deepClone(requirement);
                        clone.setVersion(newValue);
                        configurationProvider.save(clone);
                    }
                }
            }
        }
    }

    private void addAgentRepo(AgentConfiguration agentConfiguration)
    {
        agentRepositories.put(agentConfiguration.getHandle(), new ConfigurationResourceRepository(agentConfiguration));
    }

    public ResourceRepository getAgentRepository(long handle)
    {
        return agentRepositories.get(handle);
    }

    public void addDiscoveredResources(final String agentPath, final List<Resource> discoveredResources)
    {
        // Go direct to the config system.  We don't want to mess with our
        // cache here at all, because:
        //   - it may be out of date (if an event is pending); and
        //   - it will be invalidated by this change and updated by our event
        //     handler anyway
        configurationProvider.executeInsideTransaction(new NullaryFunction<Object>()
        {
            public Object process()
            {
                AgentConfiguration config = configurationProvider.get(agentPath, AgentConfiguration.class);
                if(config != null)
                {
                    Map<String, Resource> agentResources = config.getResources();
                    for (Resource r : discoveredResources)
                    {
                        addResource(agentPath, r, agentResources.get(r.getName()));
                    }
                }

                return null;
            }
        });
    }

    private void addResource(String agentPath, Resource discoveredResource, Resource existingResource)
    {
        if (existingResource == null)
        {
            configurationProvider.insert(PathUtils.getPath(agentPath, "resources"), discoveredResource);
        }
        else
        {
            existingResource = configurationProvider.deepClone(existingResource);

            // we have an existing resource, so merge the details.
            for (String propertyName: discoveredResource.getProperties().keySet())
            {
                if (!existingResource.hasProperty(propertyName))
                {
                    existingResource.addProperty(discoveredResource.getProperty(propertyName));
                }
            }

            for (String versionStr : discoveredResource.getVersions().keySet())
            {
                if (!existingResource.hasVersion(versionStr))
                {
                    existingResource.add(discoveredResource.getVersion(versionStr));
                }
                else
                {
                    ResourceVersion version = discoveredResource.getVersion(versionStr);
                    ResourceVersion existingVersion = existingResource.getVersion(versionStr);

                    for (String propertyName: version.getProperties().keySet())
                    {
                        try
                        {
                            if (!existingVersion.hasProperty(propertyName))
                            {
                                existingVersion.addProperty(version.getProperty(propertyName));
                            }
                        }
                        catch (FileLoadException e)
                        {
                            // should never happen.
                            LOG.severe(e);
                        }
                    }
                }
            }

            configurationProvider.save(existingResource);
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

    public void handleEvent(Event event)
    {
        init(((ConfigurationSystemStartedEvent)event).getConfigurationProvider());
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ ConfigurationSystemStartedEvent.class };
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }
}
