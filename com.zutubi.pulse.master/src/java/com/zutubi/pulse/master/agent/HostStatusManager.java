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
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.master.events.AgentPingEvent;
import com.zutubi.pulse.master.events.HostPingEvent;
import com.zutubi.pulse.master.events.HostUpgradeCompleteEvent;
import com.zutubi.pulse.master.events.HostUpgradeRequestedEvent;
import com.zutubi.pulse.master.model.HostState;
import com.zutubi.pulse.master.model.persistence.HostStateDao;
import com.zutubi.pulse.servercore.agent.PingStatus;
import com.zutubi.pulse.servercore.services.HostStatus;
import com.zutubi.pulse.servercore.services.UpgradeStatus;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.Map;
import java.util.TreeMap;

/**
 * Manager for maintaining the state of hosts.  Handles host pings,
 * transforming them into relevant agent pings, and manages the high-level
 * lifecycle of the host upgrade process.
 */
public class HostStatusManager implements EventListener, Stoppable
{
    private static final Logger LOG = Logger.getLogger(HostStatusManager.class);

    private final Map<Long, HostUpdater> updaters = new TreeMap<Long, HostUpdater>();

    private EventManager eventManager;
    private HostManager hostManager;
    private HostStateDao hostStateDao;
    private ObjectFactory objectFactory;

    private void handlePing(HostPingEvent hostPingEvent)
    {
        Host host = hostPingEvent.getHost();
        if (!host.isUpgrading())
        {
            HostStatus hostStatus = hostPingEvent.getHostStatus();
            for (Agent agent: hostManager.getAgentsForHost(host))
            {
                if (agent.isEnabled())
                {
                    long agentHandle = agent.getConfig().getHandle();
                    AgentPingEvent agentPingEvent = new AgentPingEvent(this, agent, hostStatus.getStatus(agentHandle), hostStatus.getRecipeId(agentHandle), hostStatus.getFreeDiskSpace(), hostStatus.isFirst(), hostStatus.getMessage());
                    eventManager.publish(agentPingEvent);
                }
            }

            if (hostStatus.getStatus() == PingStatus.VERSION_MISMATCH || hostStatus.getStatus() == PingStatus.PLUGIN_MISMATCH)
            {
                startUpdater((DefaultHost) host);
            }
        }
    }

    private void startUpdater(DefaultHost host)
    {
        synchronized (updaters)
        {
            if (!updaters.containsKey(host.getId()) && setUpgradeState(host, HostState.PersistentUpgradeState.UPGRADING))
            {
                HostUpdater updater = objectFactory.buildBean(HostUpdater.class, host, hostManager.getServiceForHost(host));
                updaters.put(host.getId(), updater);
                updater.start();
            }
        }
    }

    public void upgradeStatus(UpgradeStatus upgradeStatus)
    {
        synchronized (updaters)
        {
            // Note the field we are getting is the host id, but remains named
            // "handle" for compatibility reasons.
            HostUpdater updater = updaters.get(upgradeStatus.getHandle());
            if (updater != null)
            {
                updater.upgradeStatus(upgradeStatus);
            }
            else
            {
                LOG.warning("Received upgrade status for host that is not upgrading [" + upgradeStatus.getHandle() + "]");
            }
        }
    }

    private void handleUpgradeComplete(HostUpgradeCompleteEvent hostUpgradeCompleteEvent)
    {
        DefaultHost host = (DefaultHost) hostUpgradeCompleteEvent.getHost();
        boolean resetAgents = false;
        synchronized (updaters)
        {
            if (updaters.remove(host.getId()) != null)
            {
                if (hostUpgradeCompleteEvent.isSuccessful())
                {
                    resetAgents = setUpgradeState(host, HostState.PersistentUpgradeState.NONE);
                }
                else
                {
                    setUpgradeState(host, HostState.PersistentUpgradeState.FAILED_UPGRADE);
                }
            }
        }

        if (resetAgents)
        {
            for (Agent agent: hostManager.getAgentsForHost(host))
            {
                eventManager.publish(new AgentPingEvent(this, agent, PingStatus.OFFLINE));
            }

            hostManager.pingHost(host);
        }
    }

    private boolean setUpgradeState(DefaultHost host, HostState.PersistentUpgradeState upgradeState)
    {
        HostState hostState = hostStateDao.findById(host.getId());
        if (hostState == null)
        {
            return false;
        }
        else
        {
            hostState.setUpgradeState(upgradeState);
            hostStateDao.save(hostState);
            host.setState(hostState);
            return true;
        }
    }

    private void handleUpgradeRequested(HostUpgradeRequestedEvent hostUpgradeRequestedEvent)
    {
        startUpdater((DefaultHost) hostUpgradeRequestedEvent.getHost());
    }

    public void handleEvent(Event event)
    {
        if (event instanceof HostPingEvent)
        {
            handlePing((HostPingEvent) event);
        }
        else if (event instanceof HostUpgradeCompleteEvent)
        {
            handleUpgradeComplete((HostUpgradeCompleteEvent) event);
        }
        else
        {
            handleUpgradeRequested((HostUpgradeRequestedEvent) event);
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{HostPingEvent.class, HostUpgradeCompleteEvent.class, HostUpgradeRequestedEvent.class};
    }

    public void stop(boolean force)
    {
        if (force)
        {
            synchronized (updaters)
            {
                for (HostUpdater updater : updaters.values())
                {
                    updater.stop(force);
                }
            }
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
        eventManager.register(this);
    }

    public void setHostManager(HostManager hostManager)
    {
        this.hostManager = hostManager;
    }

    public void setHostStateDao(HostStateDao hostStateDao)
    {
        this.hostStateDao = hostStateDao;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
