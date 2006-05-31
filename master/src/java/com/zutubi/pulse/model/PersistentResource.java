package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.core.model.ResourceVersion;

/**
 */
public class PersistentResource extends Resource
{
    /**
     * The slave that owns this resource, or null if it is owned by the master.
     */
    private Slave slave;

    public PersistentResource()
    {
    }

    public PersistentResource(String name)
    {
        super(name);
    }

    public PersistentResource(String name, Slave slave)
    {
        super(name);
        this.slave = slave;
    }

    public PersistentResource(Resource resource, Slave slave)
    {
        // Shallow copy: we take ownership
        this.setName(resource.getName());
        this.setProperties(resource.getProperties());
        this.setVersions(resource.getVersions());

        this.slave = slave;
    }

    public Slave getSlave()
    {
        return slave;
    }

    public void setSlave(Slave slave)
    {
        this.slave = slave;
    }
}
