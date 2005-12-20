package com.cinnamonbob;

import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.events.build.RecipeDispatchedEvent;
import com.cinnamonbob.model.Slave;
import com.cinnamonbob.model.persistence.SlaveDao;
import com.cinnamonbob.util.logging.Logger;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

/**
 * The RecipeQueue takes in RecipeRequests and allocates them as efficiently
 * as possible to build hosts.  It is assumed all requests in the queue may
 * be handled in parallel.
 */
public class RecipeQueue
{
    private static final Logger LOG = Logger.getLogger(RecipeQueue.class);

    private List<BuildService> buildServices;
    private SlaveDao slaveDao;
    private SlaveProxyFactory slaveProxyFactory;
    private EventManager eventManager;

    public RecipeQueue()
    {
        buildServices = new LinkedList<BuildService>();
    }

    public void init()
    {
        buildServices.add(new MasterBuildService());

        for (Slave slave : slaveDao.findAll())
        {
            try
            {
                buildServices.add(new SlaveBuildService(slave, slaveProxyFactory.createProxy(slave)));
            }
            catch (MalformedURLException e)
            {
                LOG.severe("Error creating build service for slave '" + slave.getName() + "': " + e.getMessage(), e);
            }
        }
    }

    public void enqueue(RecipeDispatchRequest request)
    {
        // TODO obviously this is a skeleton: it assumes we can dispatch now!
        for (BuildService service : buildServices)
        {
            if (request.getHostRequirements().fulfilledBy(service))
            {
                service.build(request.getRequest());
                eventManager.publish(new RecipeDispatchedEvent(this, request.getRequest().getId(), service));
            }
        }
    }

    public void setSlaveDao(SlaveDao slaveDao)
    {
        this.slaveDao = slaveDao;
    }

    public void setSlaveProxyFactory(SlaveProxyFactory slaveProxyFactory)
    {
        this.slaveProxyFactory = slaveProxyFactory;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
