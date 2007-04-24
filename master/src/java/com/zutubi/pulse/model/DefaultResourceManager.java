package com.zutubi.pulse.model;

import com.zutubi.pulse.core.ConfigurableResourceRepository;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.model.persistence.BuildSpecificationNodeDao;
import com.zutubi.pulse.model.persistence.ResourceDao;
import com.zutubi.pulse.resources.ResourceDiscoverer;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 */
public class DefaultResourceManager implements ResourceManager
{
    private ResourceDao resourceDao;
    private BuildSpecificationNodeDao buildSpecificationNodeDao;
    private DatabaseResourceRepository masterResourceRepository;
    private Map<Long, DatabaseResourceRepository> slaveRepositories = new TreeMap<Long, DatabaseResourceRepository>();

    public void init()
    {
        masterResourceRepository = new DatabaseResourceRepository(resourceDao);

        ResourceDiscoverer discoverer = new ResourceDiscoverer();
        List<Resource> resources = discoverer.discover();
        addDiscoveredResources(null, resources);
    }

    public void save(PersistentResource entity)
    {
    }

    public void delete(PersistentResource entity)
    {
        // if the resource is associated with a slave, remove it manually.
        Slave slave = entity.getSlave();
        if (slave != null)
        {
            slave.getResources().remove(entity);
        }
        // now that we have removed the associations, we can delete the entity.
    }

    public PersistentResource findById(long id)
    {
        return null;
    }

    public PersistentResource findBySlaveAndName(Slave slave, String name)
    {
        return resourceDao.findBySlaveAndName(slave, name);
    }

    public ConfigurationResourceRepository getMasterRepository()
    {
        return null;
    }

    public DatabaseResourceRepository getSlaveRepository(Slave slave)
    {
        if(!slaveRepositories.containsKey(slave.getId()))
        {
            slaveRepositories.put(slave.getId(), new DatabaseResourceRepository(slave, resourceDao));
        }

        return slaveRepositories.get(slave.getId());
    }

    public void addDiscoveredResources(Slave slave, List<Resource> resources)
    {
        ConfigurableResourceRepository repository = getRepository(slave);
        // FIXME
//        for(Resource r: resources)
//        {
//            if(!repository.hasResource(r.getName()))
//            {
//                repository.addResource(r);
//            }
//        }
    }

    public List<PersistentResource> findAll()
    {
        return resourceDao.findAll();
    }

    public void editResource(PersistentResource resource, String newName, String defaultVersion)
    {
        // FIXME remember to catch config events to do this
        List<BuildSpecificationNode> nodes = buildSpecificationNodeDao.findByResourceRequirement(resource.getName());
        for(BuildSpecificationNode node: nodes)
        {
            for(ResourceRequirement r: node.getResourceRequirements())
            {
                if(r.getResource().equals(resource.getName()))
                {
                    r.setResource(newName);
                }
            }

            buildSpecificationNodeDao.save(node);
        }

        resource.setName(newName);
        resource.setDefaultVersion(defaultVersion);
    }

    public void renameResourceVersion(PersistentResource resource, String value, String newValue)
    {
        // FIXME remember to catch config events to do this
        List<BuildSpecificationNode> nodes = buildSpecificationNodeDao.findByResourceRequirement(resource.getName());
        for(BuildSpecificationNode node: nodes)
        {
            for(ResourceRequirement r: node.getResourceRequirements())
            {
                if(r.getResource().equals(resource.getName()) && value.equals(r.getVersion()))
                {
                    r.setVersion(newValue);
                }
            }

            buildSpecificationNodeDao.save(node);
        }

        ResourceVersion version = resource.getVersion(value);
        resource.deleteVersion(version);
        version.setValue(newValue);
        resource.add(version);
    }

    public void addResource(Slave slave, Resource resource)
    {
        ConfigurableResourceRepository repository = getRepository(slave);
        repository.addResource(resource, true);
    }


    public ConfigurableResourceRepository getRepository(Slave slave)
    {
        if(slave == null)
        {
            return getMasterRepository();
        }
        else
        {
            return getSlaveRepository(slave);
        }
    }

    public void setResourceDao(ResourceDao resourceDao)
    {
        this.resourceDao = resourceDao;
    }

    public void setBuildSpecificationNodeDao(BuildSpecificationNodeDao buildSpecificationNodeDao)
    {
        this.buildSpecificationNodeDao = buildSpecificationNodeDao;
    }
}
