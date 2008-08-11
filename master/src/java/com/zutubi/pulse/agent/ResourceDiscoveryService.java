package com.zutubi.pulse.agent;

import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.util.logging.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for running resource discovery on agents.  As this involves a
 * network call to the agent, it is not desirable to do it inline.
 */
public class ResourceDiscoveryService implements EventListener
{
    private static final Logger LOG = Logger.getLogger(ResourceDiscoveryService.class);

    private AtomicInteger id = new AtomicInteger(1);
    private ExecutorService executorService;
    private EventManager eventManager;
    private SlaveManager slaveManager;
    private ResourceManager resourceManager;
    private ServiceTokenManager serviceTokenManager;

    public void init()
    {
        executorService = Executors.newCachedThreadPool(new ThreadFactory()
        {
            public Thread newThread(Runnable r)
            {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("Resource Discovery Worker " + id.getAndIncrement());
                return t;
            }
        });

        eventManager.register(this);
    }

    public void handleEvent(Event e)
    {
        final AgentOnlineEvent event = (AgentOnlineEvent) e;

        executorService.execute(new Runnable()
        {
            public void run()
            {
                Agent agent = event.getAgent();
                if (agent.isSlave())
                {
                    SlaveAgent slaveAgent = (SlaveAgent) agent;
                    try
                    {
                        List<Resource> resources = slaveAgent.getSlaveService().discoverResources(serviceTokenManager.getToken());
                        resourceManager.addDiscoveredResources(slaveManager.getSlave(agent.getId()), resources);
                        eventManager.publish(new AgentResourcesDiscoveredEvent(this, slaveAgent));
                    }
                    catch (Exception e)
                    {
                        LOG.warning("Unable to discover resource for agent '" + agent.getName() + "': " + e.getMessage(), e);
                    }
                }
            }
        });
    }

    public Class[] getHandledEvents()
    {
        return new Class[] { AgentOnlineEvent.class };
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

}
