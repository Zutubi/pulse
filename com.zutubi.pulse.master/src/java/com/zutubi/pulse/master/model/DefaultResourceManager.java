/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.model;

import com.google.common.base.Predicate;
import com.zutubi.events.Event;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceVersionConfiguration;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.HostLocationFormatter;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.project.ResourceRequirementConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TypeAdapter;
import com.zutubi.tove.config.TypeListener;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.tove.events.ConfigurationSystemStartedEvent;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.NullaryFunction;

import java.util.*;

public class DefaultResourceManager implements ResourceManager, com.zutubi.events.EventListener
{
    private Map<Long, AgentResourceRepository> agentRepositories = new TreeMap<>();
    private Map<Long, ResourceConfiguration> resourcesByHandle = new HashMap<>();
    private Map<Long, ResourceVersionConfiguration> resourceVersionsByHandle = new HashMap<>();

    private ConfigurationProvider configurationProvider;
    private ConfigurationTemplateManager configurationTemplateManager;
    private AccessManager accessManager;

    private void registerConfigListeners(ConfigurationProvider configurationProvider)
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

        TypeListener<ResourceConfiguration> resourceListener = new TypeAdapter<ResourceConfiguration>(ResourceConfiguration.class)
        {
            public void save(ResourceConfiguration instance, boolean nested)
            {
                if (!nested)
                {
                    updateResource(instance);
                }
            }

            public void postInsert(ResourceConfiguration instance)
            {
                addResource(instance);
            }

            public void postDelete(ResourceConfiguration instance)
            {
                removeResource(instance);
            }

            public void postSave(ResourceConfiguration instance, boolean nested)
            {
                // Replaces the existing as it is stored by the (unchanging)
                // handle.
                addResource(instance);
            }
        };
        resourceListener.register(configurationProvider, true);

        TypeListener<ResourceVersionConfiguration> resourceVersionListener = new TypeAdapter<ResourceVersionConfiguration>(ResourceVersionConfiguration.class)
        {
            public void save(ResourceVersionConfiguration instance, boolean nested)
            {
                if (!nested)
                {
                    updateResourceVersion(instance);
                }
            }

            public void postInsert(ResourceVersionConfiguration instance)
            {
                addResourceVersion(instance);
            }

            public void postDelete(ResourceVersionConfiguration instance)
            {
                removeResourceVersion(instance);
            }

            public void postSave(ResourceVersionConfiguration instance, boolean nested)
            {
                // Replaces the existing as it is stored by the (unchanging)
                // handle.
                addResourceVersion(instance);
            }
        };
        resourceVersionListener.register(configurationProvider, true);
    }

    public void init()
    {
        for (ResourceConfiguration resource : configurationProvider.getAll(ResourceConfiguration.class))
        {
            addResource(resource);
        }

        for (ResourceVersionConfiguration resourceVersion : configurationProvider.getAll(ResourceVersionConfiguration.class))
        {
            addResourceVersion(resourceVersion);
        }

        for (AgentConfiguration agentConfig : configurationProvider.getAll(AgentConfiguration.class))
        {
            addAgentRepo(agentConfig);
        }
    }

    private void addResource(ResourceConfiguration resource)
    {
        resourcesByHandle.put(resource.getHandle(), resource);
    }

    private void removeResource(ResourceConfiguration resource)
    {
        resourcesByHandle.remove(resource.getHandle());
    }

    private void updateResource(ResourceConfiguration resource)
    {
        ResourceConfiguration oldResource = resourcesByHandle.get(resource.getHandle());
        if (oldResource != null)
        {
            String oldName = oldResource.getName();
            String newName = resource.getName();
            if (!oldName.equals(newName))
            {
                for (ResourceRequirementConfiguration requirement : configurationProvider.getAll(ResourceRequirementConfiguration.class))
                {
                    if (requirement.getResource().equals(oldName))
                    {
                        ResourceRequirementConfiguration clone = configurationProvider.deepClone(requirement);
                        clone.setResource(newName);
                        configurationProvider.save(clone);
                    }
                }
            }
        }
    }

    private void addResourceVersion(ResourceVersionConfiguration resourceVersion)
    {
        resourceVersionsByHandle.put(resourceVersion.getHandle(), resourceVersion);
    }

    private void removeResourceVersion(ResourceVersionConfiguration resourceVersion)
    {
        resourceVersionsByHandle.remove(resourceVersion.getHandle());
    }

    private void updateResourceVersion(ResourceVersionConfiguration resourceVersion)
    {
        ResourceVersionConfiguration oldVersion = resourceVersionsByHandle.get(resourceVersion.getHandle());
        if (oldVersion != null)
        {
            String oldValue = oldVersion.getValue();
            String newValue = resourceVersion.getValue();
            if (!oldValue.equals(newValue))
            {
                ResourceConfiguration owningResource = configurationProvider.getAncestorOfType(resourceVersion, ResourceConfiguration.class);
                if (Objects.equals(owningResource.getDefaultVersion(), oldValue))
                {
                    ResourceConfiguration clone = configurationProvider.deepClone(owningResource);
                    clone.setDefaultVersion(newValue);
                    configurationProvider.save(clone);
                }

                String resourceName = owningResource.getName();
                for (ResourceRequirementConfiguration requirement : configurationProvider.getAll(ResourceRequirementConfiguration.class))
                {
                    if (Objects.equals(requirement.getResource(), resourceName) && Objects.equals(requirement.getVersion(), oldValue))
                    {
                        ResourceRequirementConfiguration clone = configurationProvider.deepClone(requirement);
                        clone.setVersion(newValue);
                        configurationProvider.save(clone);
                    }
                }
            }
        }
    }

    private void addAgentRepo(AgentConfiguration agentConfiguration)
    {
        agentRepositories.put(agentConfiguration.getHandle(), new AgentResourceRepository(agentConfiguration));
    }

    public ResourceRepository getAgentRepository(long handle)
    {
        return agentRepositories.get(handle);
    }

    public ResourceRepository getAgentRepository(AgentConfiguration agent)
    {
        return agentRepositories.get(agent.getHandle());
    }

    public ResourceRepository getAgentRepository(Agent agent)
    {
        return getAgentRepository(agent.getConfig());
    }

    public Set<AgentConfiguration> getCapableAgents(Collection<? extends ResourceRequirement> requirements)
    {
        Set<AgentConfiguration> agents = new HashSet<>();
        for (AgentResourceRepository repository: agentRepositories.values())
        {
            if (repository.satisfies(requirements))
            {
                agents.add(repository.getAgentConfig());
            }
        }

        return agents;
    }

    public List<AgentConfiguration> addDiscoveredResources(final String hostLocation, final List<ResourceConfiguration> discoveredResources)
    {
        // Go direct to the config system.  We don't want to mess with our
        // cache here at all, because:
        //   - it may be out of date (if an event is pending); and
        //   - it will be invalidated by this change and updated by our event
        //     handler anyway
        return configurationProvider.executeInsideTransaction(new NullaryFunction<List<AgentConfiguration>>()
        {
            public List<AgentConfiguration> process()
            {
                if (hostLocation.equals(HostLocationFormatter.LOCATION_MASTER))
                {
                    GlobalConfiguration globalConfiguration = configurationProvider.get(GlobalConfiguration.class);
                    for (ResourceConfiguration discoveredResource : discoveredResources)
                    {
                        ResourceConfiguration existingResource = globalConfiguration.getResources().get(discoveredResource.getName());
                        addResource(globalConfiguration.getConfigurationPath(), discoveredResource, existingResource);
                        // Lookup again, we just changed this config.
                        globalConfiguration = configurationProvider.get(GlobalConfiguration.class);
                    }
                }

                Collection<AgentConfiguration> agentConfigs = configurationTemplateManager.getHighestInstancesSatisfying(new DefinesLocation(hostLocation), AgentConfiguration.class);
                List<AgentConfiguration> affectedAgents = new LinkedList<>();
                for (AgentConfiguration config: agentConfigs)
                {
                    String agentPath = config.getConfigurationPath();
                    for (ResourceConfiguration discoveredResource : discoveredResources)
                    {
                        // Check that no descendant defines the resource, so we
                        // don't conflict with those definitions.
                        if (noDescendantDefinesResource(agentPath, discoveredResource.getName()))
                        {
                            Map<String, ResourceConfiguration> agentResources = config.getResources();
                            addResource(agentPath, discoveredResource, agentResources.get(discoveredResource.getName()));

                            // Lookup again, we just changed this agent.
                            config = configurationProvider.get(agentPath, AgentConfiguration.class);
                        }
                    }

                    affectedAgents.add(config);
                }

                return affectedAgents;
            }
        });
    }

    private boolean noDescendantDefinesResource(String agentPath, String resourceName)
    {
        Set<AgentConfiguration> descendants = configurationProvider.getAllDescendants(agentPath, AgentConfiguration.class, true, false);
        for (AgentConfiguration descendant: descendants)
        {
            if (descendant.getResources().containsKey(resourceName))
            {
                return false;
            }
        }

        return true;
    }

    private void addResource(String ownerPath, ResourceConfiguration discoveredResource, ResourceConfiguration existingResource)
    {
        if (existingResource == null)
        {
            configurationProvider.insert(PathUtils.getPath(ownerPath, "resources"), discoveredResource);
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
                    existingResource.addVersion(discoveredResource.getVersion(versionStr));
                }
                else
                {
                    ResourceVersionConfiguration version = discoveredResource.getVersion(versionStr);
                    ResourceVersionConfiguration existingVersion = existingResource.getVersion(versionStr);

                    for (String propertyName: version.getProperties().keySet())
                    {
                        if (!existingVersion.hasProperty(propertyName))
                        {
                            existingVersion.addProperty(version.getProperty(propertyName));
                        }
                    }
                }
            }

            configurationProvider.save(existingResource);
        }
    }

    public Map<String, List<ResourceConfiguration>> findAllVisible()
    {
        Map<String, List<ResourceConfiguration>> allResources = new HashMap<>();
        for (AgentResourceRepository repo : agentRepositories.values())
        {
            if (accessManager.hasPermission(AccessManager.ACTION_VIEW, repo.getAgentConfig()))
            {
                allResources.put(repo.getAgentConfig().getName(), new LinkedList<>(repo.getAll().values()));
            }
        }

        return allResources;
    }

    public void handleEvent(Event event)
    {
        if (event instanceof ConfigurationEventSystemStartedEvent)
        {
            registerConfigListeners(((ConfigurationEventSystemStartedEvent)event).getConfigurationProvider());
        }
        else
        {
            init();
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ ConfigurationEventSystemStartedEvent.class, ConfigurationSystemStartedEvent.class };
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }

    private static class DefinesLocation implements Predicate<AgentConfiguration>
    {
        private String location;

        public DefinesLocation(String location)
        {
            this.location = location;
        }


        public boolean apply(AgentConfiguration configuration)
        {
            return HostLocationFormatter.format(configuration).equals(location);
        }
    }
}
