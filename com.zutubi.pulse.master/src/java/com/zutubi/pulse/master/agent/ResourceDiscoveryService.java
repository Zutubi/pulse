/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.agent;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.master.events.AgentOnlineEvent;
import com.zutubi.pulse.master.events.AgentResourcesDiscoveredEvent;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.util.logging.Logger;

import java.util.Collection;
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
    private AgentManager agentManager;
    private EventManager eventManager;
    private HostManager hostManager;
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
                Host host = event.getAgent().getHost();
                try
                {
                    HostService service = hostManager.getServiceForHost(host);
                    List<ResourceConfiguration> resources = service.discoverResources();
                    Collection<AgentConfiguration> affectedAgents = resourceManager.addDiscoveredResources(host.getLocation(), resources);
                    for (AgentConfiguration agentConfig: affectedAgents)
                    {
                        Agent agent = agentManager.getAgent(agentConfig);
                        if (agent != null)
                        {
                            eventManager.publish(new AgentResourcesDiscoveredEvent(this, agent));
                        }
                    }
                }
                catch (Exception e)
                {
                    LOG.warning("Unable to discover resource for host '" + host.getLocation() + "': " + e.getMessage(), e);
                }
            }
        });
    }

    public Class[] getHandledEvents()
    {
        return new Class[] { AgentOnlineEvent.class };
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setHostManager(HostManager hostManager)
    {
        this.hostManager = hostManager;
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
