package com.zutubi.pulse.master.agent;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.master.events.AgentOnlineEvent;
import com.zutubi.pulse.master.events.AgentResourcesDiscoveredEvent;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.util.logging.Logger;

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
    private ResourceManager resourceManager;
    private ThreadFactory threadFactory;

    public void init()
    {
        executorService = Executors.newCachedThreadPool(new ThreadFactory()
        {
            public Thread newThread(Runnable r)
            {
                Thread t = threadFactory.newThread(r);
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
                try
                {
                    List<ResourceConfiguration> resources = agent.getService().discoverResources();
                    resourceManager.addDiscoveredResources(agent.getConfig().getConfigurationPath(), resources);
                    eventManager.publish(new AgentResourcesDiscoveredEvent(this, agent));
                }
                catch (Exception e)
                {
                    LOG.warning("Unable to discover resource for agent '" + agent.getConfig().getName() + "': " + e.getMessage(), e);
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

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }
}
