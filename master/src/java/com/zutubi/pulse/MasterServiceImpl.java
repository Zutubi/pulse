package com.zutubi.pulse;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.SlaveEvent;
import com.zutubi.pulse.services.MasterService;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.PersistentResource;
import com.zutubi.pulse.model.persistence.ResourceDao;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.bootstrap.ComponentContext;

import java.util.List;

/**
 */
public class MasterServiceImpl implements MasterService
{
    private EventManager eventManager;
    private ResourceManager resourceManager;
    private SlaveManager slaveManager;

    public void handleEvent(Event event)
    {
        eventManager.publish(event);
    }

    public Resource getResource(long slaveId, String name)
    {
        Slave slave = getSlaveManager().getSlave(slaveId);
        PersistentResource persistent;
        Resource resource = null;

        if(slave != null)
        {
            persistent = getResourceManager().findBySlaveAndName(slave, name);
            if(persistent != null)
            {
                resource = persistent.asResource();
            }
        }

        return resource;
    }

    public List<String> getResourceNames(long slaveId)
    {
        Slave slave = getSlaveManager().getSlave(slaveId);
        if(slave != null)
        {
            return getResourceManager().getSlaveRepository(slave).getResourceNames();
        }

        return null;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }

    public SlaveManager getSlaveManager()
    {
        if(slaveManager == null)
        {
            ComponentContext.autowire(this);
        }
        return slaveManager;
    }

    public ResourceManager getResourceManager()
    {
        if(slaveManager == null)
        {
            ComponentContext.autowire(this);
        }
        return resourceManager;
    }
}
