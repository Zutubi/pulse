package com.zutubi.pulse;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.services.MasterService;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.services.InvalidTokenException;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.PersistentResource;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.bootstrap.ComponentContext;

import java.util.List;

/**
 */
public class MasterServiceImpl implements MasterService
{
    private ServiceTokenManager serviceTokenManager;
    private EventManager eventManager;
    private ResourceManager resourceManager;
    private SlaveManager slaveManager;

    public void handleEvent(String token, Event event) throws InvalidTokenException
    {
        getServiceTokenManager().validateToken(token);
        eventManager.publish(event);
    }

    public Resource getResource(String token, long slaveId, String name) throws InvalidTokenException
    {
        getServiceTokenManager().validateToken(token);

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

    public List<String> getResourceNames(String token, long slaveId) throws InvalidTokenException
    {
        getServiceTokenManager().validateToken(token);

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

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

    private SlaveManager getSlaveManager()
    {
        if(slaveManager == null)
        {
            ComponentContext.autowire(this);
        }
        return slaveManager;
    }

    private ResourceManager getResourceManager()
    {
        if(slaveManager == null)
        {
            ComponentContext.autowire(this);
        }
        return resourceManager;
    }

    public ServiceTokenManager getServiceTokenManager()
    {
        if(serviceTokenManager == null)
        {
            ComponentContext.autowire(this);
        }

        return serviceTokenManager;
    }
}
