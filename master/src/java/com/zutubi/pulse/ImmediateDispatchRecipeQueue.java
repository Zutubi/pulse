package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.SlaveAvailableEvent;
import com.zutubi.pulse.events.build.RecipeDispatchedEvent;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.util.logging.Logger;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

/**
 * The RecipeQueue takes in RecipeRequests and allocates them as efficiently
 * as possible to build hosts.  It is assumed all requests in the queue may
 * be handled in parallel.
 */
public class ImmediateDispatchRecipeQueue implements RecipeQueue, EventListener
{
    private static final Logger LOG = Logger.getLogger(ImmediateDispatchRecipeQueue.class);

    private List<BuildService> buildServices;
    private SlaveManager slaveManager;
    private SlaveProxyFactory slaveProxyFactory;
    private EventManager eventManager;
    private ObjectFactory objectFactory;

    public ImmediateDispatchRecipeQueue()
    {
        buildServices = new LinkedList<BuildService>();
    }

    public void init()
    {
        try
        {
            MasterBuildService buildService = objectFactory.buildBean(MasterBuildService.class);
            buildServices.add(buildService);
        }
        catch (Exception e)
        {
            LOG.error(e);
        }

        for (Slave slave : slaveManager.getAll())
        {
            SlaveBuildService slaveService = createSlaveService(slave);
            if (slaveService != null)
            {
                buildServices.add(slaveService);
            }
        }

        eventManager.register(this);
    }

    private SlaveBuildService createSlaveService(Slave slave)
    {
        try
        {
            SlaveBuildService buildService = new SlaveBuildService(slave, slaveProxyFactory.createProxy(slave));
            ComponentContext.autowire(buildService);
            return buildService;
        }
        catch (MalformedURLException e)
        {
            LOG.severe("Error creating build service for slave '" + slave.getName() + "': " + e.getMessage(), e);
        }

        return null;
    }

    public void enqueue(RecipeDispatchRequest request)
    {
        // This assumes we can dispatch now, which is unlikely to be a
        // practical dispatch algorithm!
        for (BuildService service : buildServices)
        {
            if (request.getHostRequirements().fulfilledBy(service))
            {
                try
                {
                    request.getRequest().prepare();
                }
                catch (PulseException e)
                {
                    throw new BuildException(e);
                }

                service.build(request.getRequest());
                eventManager.publish(new RecipeDispatchedEvent(this, request.getRequest(), service));
                return;
            }
        }

        throw new BuildException("No build service found to handle request.");
    }

    public List<RecipeDispatchRequest> takeSnapshot()
    {
        // Never actually queue anything!
        return new LinkedList<RecipeDispatchRequest>();
    }

    public boolean cancelRequest(long id)
    {
        return false;
    }

    public void start()
    {
    }

    public void stop()
    {
    }

    public boolean isRunning()
    {
        return true;
    }

    public void handleEvent(Event evt)
    {
        SlaveAvailableEvent event = (SlaveAvailableEvent) evt;
        SlaveBuildService newService = createSlaveService(event.getSlave());

        if (newService != null)
        {
            for (BuildService service : buildServices)
            {
                if (service.equals(newService))
                {
                    return;
                }
            }
        }

        buildServices.add(newService);
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{SlaveAvailableEvent.class};
    }

    public void setSlaveProxyFactory(SlaveProxyFactory slaveProxyFactory)
    {
        this.slaveProxyFactory = slaveProxyFactory;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
