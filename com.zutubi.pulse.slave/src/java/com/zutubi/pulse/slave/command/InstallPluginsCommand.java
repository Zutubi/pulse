package com.zutubi.pulse.slave.command;

import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.repository.PluginInfo;
import com.zutubi.pulse.core.plugins.repository.PluginRepository;
import com.zutubi.pulse.core.plugins.repository.http.HttpPluginRepository;
import com.zutubi.pulse.core.plugins.sync.PluginSynchroniser;
import com.zutubi.pulse.core.plugins.sync.SynchronisationActions;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;

import java.util.List;

/**
 * A command used to install new plugins that have been found on the master.
 * This is simpler than a full plugin sync as no restart is required.
 */
public class InstallPluginsCommand implements Runnable
{
    private static final Logger LOG = Logger.getLogger(InstallPluginsCommand.class);

    private String masterUrl;
    private PluginManager pluginManager;
    private PluginSynchroniser pluginSynchroniser;

    public InstallPluginsCommand(String masterUrl)
    {
        this.masterUrl = masterUrl;
    }

    public void run()
    {
        try
        {
            HttpPluginRepository repository = new HttpPluginRepository(StringUtils.join("/", true, masterUrl, "pluginrepository/"));
            List<PluginInfo> availablePlugins = repository.getAvailablePlugins(PluginRepository.Scope.SERVER);
            synchronized (pluginManager)
            {
                SynchronisationActions requiredActions = pluginSynchroniser.determineRequiredActions(availablePlugins);
                if (!requiredActions.isRebootRequired())
                {
                    pluginSynchroniser.synchronise(repository, requiredActions);
                }        
            }
        }
        catch (Exception e)
        {
            LOG.warning("Unable to install new plugins: " + e.getMessage(), e);
        }
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }

    public void setPluginSynchroniser(PluginSynchroniser pluginSynchroniser)
    {
        this.pluginSynchroniser = pluginSynchroniser;
    }
}
