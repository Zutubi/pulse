package com.zutubi.pulse.model;

import com.zutubi.pulse.core.config.Resource;

import java.util.List;

/**
 */
public interface ResourceManager
{
    PersistentResource findBySlaveAndName(Slave slave, String name);

    ConfigurationResourceRepository getMasterRepository();
    DatabaseResourceRepository getSlaveRepository(Slave slave);

    void addDiscoveredResources(Slave slave, List<Resource> resources);

    List<PersistentResource> findAll();

    void editResource(PersistentResource resource, String newName, String defaultVersion);
    void renameResourceVersion(PersistentResource resource, String value, String newValue);

    void addResource(Slave slave, Resource resource);
}
