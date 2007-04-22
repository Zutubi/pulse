package com.zutubi.pulse.model;

import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.ConfigurableResourceRepository;
import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceVersion;
import com.zutubi.pulse.model.persistence.ResourceDao;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class DatabaseResourceRepository implements ConfigurableResourceRepository
{
    private Slave slave;
    private ResourceRepository parent;
    private ResourceDao resourceDao;

    public DatabaseResourceRepository(ResourceDao resourceDao)
    {
        slave = null;
        this.resourceDao = resourceDao;
    }

    public DatabaseResourceRepository(Slave slave, ResourceDao resourceDao)
    {
        this.slave = slave;
        this.resourceDao = resourceDao;
    }

    public boolean hasResource(String name, String version)
    {
        Resource r = getResource(name);
        return r != null && (version == null || r.hasVersion(version));
    }

    public boolean hasResource(String name)
    {
        return getResource(name) != null;
    }

    public Resource getResource(String name)
    {
        if (parent != null && parent.hasResource(name))
        {
            return parent.getResource(name);
        }
        else
        {
            return resourceDao.findBySlaveAndName(slave, name);
        }
    }

    public List<String> getResourceNames()
    {
        // We assume there are not very many resources defined: maybe tens at most
        List<String> names;

        if (parent != null)
        {
            names = parent.getResourceNames();
        }
        else
        {
            names = new LinkedList<String>();
        }

        for (PersistentResource resource : resourceDao.findAllBySlave(slave))
        {
            if (!names.contains(resource.getName()))
            {
                names.add(resource.getName());
            }
        }

        return names;
    }

    public void setParent(ResourceRepository parent)
    {
        this.parent = parent;
    }

    public void addResource(Resource resource)
    {
        addResource(resource, false);
    }

    public void addResource(Resource resource, boolean overwrite)
    {
        // merge this new resource with existing resources.  The overwrite refers to properties that already exist.

        PersistentResource existingResource = resourceDao.findBySlaveAndName(slave, resource.getName());
        if (existingResource == null)
        {
            resourceDao.save(new PersistentResource(resource, slave));
            return;
        }

        // we have an existing resource, so merge the details.
        for (String propertyName: resource.getProperties().keySet())
        {
            if (existingResource.hasProperty(propertyName) && overwrite)
            {
                existingResource.deleteProperty(propertyName);
                existingResource.addProperty(resource.getProperty(propertyName));
            }
            else if (!existingResource.hasProperty(propertyName))
            {
                existingResource.addProperty(resource.getProperty(propertyName));
            }
        }

        for (String versionStr : resource.getVersions().keySet())
        {
            if (!existingResource.hasVersion(versionStr))
            {
                existingResource.add(resource.getVersion(versionStr));
            }
            else
            {
                ResourceVersion version = resource.getVersion(versionStr);
                ResourceVersion existingVersion = existingResource.getVersion(versionStr);

                for (String propertyName: version.getProperties().keySet())
                {
                    try
                    {
                        if (existingVersion.hasProperty(propertyName) && overwrite)
                        {
                            existingVersion.deleteProperty(propertyName);
                            existingVersion.addProperty(version.getProperty(propertyName));
                        }
                        else if (!existingVersion.hasProperty(propertyName))
                        {
                            existingVersion.addProperty(version.getProperty(propertyName));
                        }
                    }
                    catch (FileLoadException e)
                    {
                        // should never happen.
                        e.printStackTrace();
                    }
                }
            }
        }
        resourceDao.save(existingResource);
    }
}
