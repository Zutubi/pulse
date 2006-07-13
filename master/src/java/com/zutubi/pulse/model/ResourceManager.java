package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Resource;

import java.util.List;

/**
 */
public interface ResourceManager extends EntityManager<PersistentResource>
{
    PersistentResource findById(long id);
    List<PersistentResource> findBySlave(Slave slave);
    PersistentResource findBySlaveAndName(Slave slave, String name);

    DatabaseResourceRepository getMasterRepository();
    DatabaseResourceRepository getSlaveRepository(Slave slave);

    void addDiscoveredResources(Slave slave, List<Resource> resources);

    List<PersistentResource> findAll();
}
