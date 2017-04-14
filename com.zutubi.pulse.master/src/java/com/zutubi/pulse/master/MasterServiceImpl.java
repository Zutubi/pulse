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

package com.zutubi.pulse.master;

import com.google.common.collect.Iterables;
import com.zutubi.events.Event;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.PluginRunningPredicate;
import com.zutubi.pulse.core.plugins.repository.PluginInfo;
import com.zutubi.pulse.core.plugins.repository.PluginList;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.agent.HostStatusManager;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;
import com.zutubi.pulse.servercore.services.InvalidTokenException;
import com.zutubi.pulse.servercore.services.MasterService;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.pulse.servercore.services.UpgradeStatus;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of the Master Hessian interface.  Used by agents to communicate with the master.
 */
public class MasterServiceImpl implements MasterService
{
    private AtomicBoolean systemStarted = new AtomicBoolean(false);

    private ServiceTokenManager serviceTokenManager;
    private EventManager eventManager;
    private ResourceManager resourceManager;
    private HostStatusManager hostStatusManager;
    private PluginManager pluginManager;

    public List<PluginInfo> pong()
    {
        if (systemStarted.get())
        {
            return PluginList.toInfos(Iterables.filter(pluginManager.getPlugins(), new PluginRunningPredicate()));
        }
        else
        {
            throw new PulseRuntimeException("Master service still starting.");
        }
    }

    public void upgradeStatus(String token, UpgradeStatus upgradeStatus)
    {
        if (validateToken(token))
        {
            hostStatusManager.upgradeStatus(upgradeStatus);
        }
    }

    public void handleEvent(String token, Event event) throws InvalidTokenException
    {
        if (validateToken(token))
        {
            eventManager.publish(event);
        }
    }

    public ResourceConfiguration getResource(String token, long agentHandle, String name) throws InvalidTokenException
    {
        if (validateToken(token))
        {
            ResourceRepository repository = resourceManager.getAgentRepository(agentHandle);
            if (repository != null)
            {
                return repository.getResource(name);
            }
        }

        return null;
    }

    private boolean validateToken(String token)
    {
        if (systemStarted.get())
        {
            serviceTokenManager.validateToken(token);
            return true;
        }
        else
        {
            // CIB-1019: agents can send messages during master startup
            return false;
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
        eventManager.register(new SystemStartedListener()
        {
            public void systemStarted()
            {
                SpringComponentContext.autowire(MasterServiceImpl.this);
                systemStarted.set(true);
            }
        });
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

    public void setHostStatusManager(HostStatusManager hostStatusManager)
    {
        this.hostStatusManager = hostStatusManager;
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
