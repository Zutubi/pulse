package com.zutubi.pulse.model;

import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.model.persistence.ResourceDao;
import com.zutubi.pulse.ResourceDiscoverer;

import java.util.Map;
import java.util.TreeMap;
import java.util.List;

/**
 */
public class DefaultResourceManager implements ResourceManager
{
    private ResourceDao resourceDao;
    private DatabaseResourceRepository masterResourceRepository;
    private Map<Long, DatabaseResourceRepository> slaveRepositories = new TreeMap<Long, DatabaseResourceRepository>();

    public void init()
    {
        masterResourceRepository = new DatabaseResourceRepository(resourceDao);

        ResourceDiscoverer discoverer = new ResourceDiscoverer();
        List<Resource> resources = discoverer.discover();
        for(Resource r: resources)
        {
            if(!masterResourceRepository.hasResource(r.getName()))
            {
                PersistentResource persistent = new PersistentResource(r, null);
                masterResourceRepository.addResource(persistent);
            }
        }
    }

    public void save(PersistentResource entity)
    {
        resourceDao.save(entity);
    }

    public void delete(PersistentResource entity)
    {
        resourceDao.delete(entity);
    }

    public PersistentResource findById(long id)
    {
        return resourceDao.findById(id);
    }

    public PersistentResource findBySlaveAndName(Slave slave, String name)
    {
        return resourceDao.findBySlaveAndName(slave, name);
    }

    public DatabaseResourceRepository getMasterRepository()
    {
        return masterResourceRepository;
    }

    public DatabaseResourceRepository getSlaveRepository(Slave slave)
    {
        if(slaveRepositories.containsKey(slave.getId()))
        {
            slaveRepositories.put(slave.getId(), new DatabaseResourceRepository(slave, resourceDao));
        }

        return slaveRepositories.get(slave.getId());
    }

    public List<PersistentResource> findBySlave(Slave slave)
    {
        return resourceDao.findAllBySlave(slave);
    }

    public DatabaseResourceRepository getRepository(Slave slave)
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
}
