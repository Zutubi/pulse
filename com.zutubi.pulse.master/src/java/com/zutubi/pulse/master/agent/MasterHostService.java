package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.plugins.ResourceLocatorExtensionManager;
import com.zutubi.pulse.core.resources.ResourceDiscoverer;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.servercore.ServerInfoModel;
import com.zutubi.pulse.servercore.ServerRecipeService;
import com.zutubi.pulse.servercore.bootstrap.StartupManager;
import com.zutubi.pulse.servercore.services.HostStatus;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
import com.zutubi.pulse.servercore.util.logging.ServerMessagesHandler;
import com.zutubi.util.logging.Logger;

import java.util.List;

/**
 * A service for communicating with the master's internal host.
 */
public class MasterHostService implements HostService
{
    private static final Logger LOG = Logger.getLogger(MasterHostService.class);

    private MasterConfigurationManager configurationManager;
    private ServerRecipeService serverRecipeService;
    private StartupManager startupManager;
    private ServerMessagesHandler serverMessagesHandler;
    private ResourceLocatorExtensionManager resourceLocatorExtensionManager;

    public int ping()
    {
        return Version.getVersion().getBuildNumberAsInt();
    }

    public HostStatus getStatus(String masterLocation)
    {
        return new HostStatus(serverRecipeService.getBuildingRecipes(), false);
    }

    public ServerInfoModel getSystemInfo(boolean includeDetailed)
    {
        return ServerInfoModel.getServerInfo(configurationManager, startupManager, includeDetailed);
    }

    public List<CustomLogRecord> getRecentMessages()
    {
        return serverMessagesHandler.takeSnapshot();
    }

    public void garbageCollect()
    {
        Runtime.getRuntime().gc();
    }

    public List<ResourceConfiguration> discoverResources()
    {
        ResourceDiscoverer discoverer = resourceLocatorExtensionManager.createResourceDiscoverer();
        return discoverer.discover();
    }

    public boolean updateVersion(String masterBuild, String masterUrl, long handle, String packageUrl, long packageSize)
    {
        LOG.warning("Illegal request to update version of master host.");
        return true;
    }

    public boolean syncPlugins(String masterUrl, long hostId, String pluginRepositoryUrl)
    {
        return false;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setServerRecipeService(ServerRecipeService serverRecipeService)
    {
        this.serverRecipeService = serverRecipeService;
    }

    public void setStartupManager(StartupManager startupManager)
    {
        this.startupManager = startupManager;
    }

    public void setServerMessagesHandler(ServerMessagesHandler serverMessagesHandler)
    {
        this.serverMessagesHandler = serverMessagesHandler;
    }

    public void setResourceLocatorExtensionManager(ResourceLocatorExtensionManager resourceLocatorExtensionManager)
    {
        this.resourceLocatorExtensionManager = resourceLocatorExtensionManager;
    }
}
