package com.zutubi.pulse.model;

import com.zutubi.prototype.config.CollectionListener;
import com.zutubi.prototype.config.ConfigurationEventListener;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.config.events.PostInsertEvent;
import com.zutubi.prototype.config.events.PostSaveEvent;
import com.zutubi.prototype.config.events.PreDeleteEvent;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.pulse.core.ConfigurableResourceRepository;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class DefaultResourceManager implements ResourceManager, ConfigurationEventListener
{
    private Map<Long, ConfigurationResourceRepository> agentRepositories = new TreeMap<Long, ConfigurationResourceRepository>();

    private ConfigurationProvider configurationProvider;

    public void init()
    {
        CollectionListener<AgentConfiguration> listener = new CollectionListener<AgentConfiguration>("agent", AgentConfiguration.class, true)
        {
            protected void preInsert(MutableRecord record)
            {
            }

            protected void instanceInserted(AgentConfiguration instance)
            {
                agentRepositories.put(instance.getHandle(), new ConfigurationResourceRepository(instance, configurationProvider));
            }

            protected void instanceDeleted(AgentConfiguration instance)
            {
                agentRepositories.remove(instance.getHandle());
            }

            protected void instanceChanged(AgentConfiguration instance)
            {
                instanceDeleted(instance);
                instanceInserted(instance);
            }
        };

        listener.register(configurationProvider);
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
        // FIXME
        return null;
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

    public void handleConfigurationEvent(ConfigurationEvent event)
    {
        if(event instanceof PostInsertEvent)
        {
            PostInsertEvent pie = (PostInsertEvent) event;
            AgentConfiguration agentConfig = (AgentConfiguration) pie.getNewInstance();
            agentRepositories.put(agentConfig.getHandle(), new ConfigurationResourceRepository((AgentConfiguration) pie.getNewInstance(), configurationProvider));
        }
        else if(event instanceof PreDeleteEvent)
        {
            PreDeleteEvent pde = (PreDeleteEvent) event;
            agentRepositories.remove(((AgentConfiguration) pde.getInstance()).getHandle());
        }
        else if(event instanceof PostSaveEvent)
        {
            
        }
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
