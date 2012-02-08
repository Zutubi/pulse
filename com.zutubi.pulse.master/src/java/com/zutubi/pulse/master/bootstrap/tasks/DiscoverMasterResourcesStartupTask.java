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
