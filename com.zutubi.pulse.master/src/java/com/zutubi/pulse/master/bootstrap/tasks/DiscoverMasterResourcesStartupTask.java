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

package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.core.plugins.ResourceLocatorExtensionManager;
import com.zutubi.pulse.core.resources.ResourceDiscoverer;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.Host;
import com.zutubi.pulse.master.agent.HostLocationFormatter;
import com.zutubi.pulse.master.agent.HostManager;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;

import java.util.Collection;

/**
 * A startup task that runs resource discovery on the Pulse master if there are
 * no enabled master agents defined.
 */
public class DiscoverMasterResourcesStartupTask implements StartupTask
{
    private HostManager hostManager;
    private ResourceLocatorExtensionManager resourceLocatorExtensionManager;
    private ResourceManager resourceManager;

    public void execute() throws Exception
    {
        Host host = hostManager.getHostForLocation(HostLocationFormatter.LOCATION_MASTER);
        if (host != null)
        {
            Collection<Agent> agents = hostManager.getAgentsForHost(host);
            for (Agent agent: agents)
            {
                if (agent.isEnabled())
                {
                    // Found a master agent that is enabled, we'll rely on its discovery.
                    return;
                }
            }
        }

        ResourceDiscoverer discoverer = resourceLocatorExtensionManager.createResourceDiscoverer();
        resourceManager.addDiscoveredResources(HostLocationFormatter.LOCATION_MASTER, discoverer.discover());
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void setHostManager(HostManager hostManager)
    {
        this.hostManager = hostManager;
    }

    public void setResourceLocatorExtensionManager(ResourceLocatorExtensionManager resourceLocatorExtensionManager)
    {
        this.resourceLocatorExtensionManager = resourceLocatorExtensionManager;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }
}
