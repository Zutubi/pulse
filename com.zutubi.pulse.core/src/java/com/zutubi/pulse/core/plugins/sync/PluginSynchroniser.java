package com.zutubi.pulse.core.plugins.sync;

import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginException;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.PluginRunningPredicate;
import com.zutubi.pulse.core.plugins.repository.PluginInfo;
import com.zutubi.pulse.core.plugins.repository.PluginRepository;
import com.zutubi.pulse.core.plugins.repository.PluginScopePredicate;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * A service to synchronise plugins with a repository.
 */
public class PluginSynchroniser
{
    private PluginManager pluginManager;

    /**
     * Indicates if a reboot would be required to synchronise with the given
     * plugins.
     * 
     * @param repositoryPlugins list of plugins to test against (all scopes)
     * @param scope             scope of plugins we want to synchronise with
     * @return true if a reboot would be required to synchronise with the given
     *         plugins
     */
    public boolean isRebootRequired(List<PluginInfo> repositoryPlugins, PluginRepository.Scope scope)
    {
        repositoryPlugins = CollectionUtils.filter(repositoryPlugins, new PluginScopePredicate(scope));
        return determineRequiredActions(repositoryPlugins).isRebootRequired(); 
    }

    /**
     * Synchronises the plugins installed locally with those in the given
     * repository.  This may require a restart of the plugin system.
     * 
     * @param repository the repository to synchronise with
     * @param scope      the scope of plugins we want to synchronise with
     * @return true if a reboot is required to complete synchronisation, false
     *         if it was able to complete without a reboot
     * @throws PluginException on any error retrieving or working with a plugin
     */
    public boolean synchroniseWithRepository(final PluginRepository repository, PluginRepository.Scope scope) throws PluginException
    {
        List<PluginInfo> repositoryPlugins = repository.getAvailablePlugins(scope);
        SyncActions syncActions;
        synchronized (pluginManager)
        {
            syncActions = determineRequiredActions(repositoryPlugins);
            if (syncActions.isRebootRequired())
            {
                for (PluginInfo pluginInfo: syncActions.toInstall)
                {
                    pluginManager.requestInstall(repository.getPluginLocation(pluginInfo));
                }
                
                for (PluginInfo pluginInfo: syncActions.toUpgrade)
                {
                    pluginManager.getPlugin(pluginInfo.getId()).upgrade(repository.getPluginLocation(pluginInfo));
                }
            
                for (String id: syncActions.toUninstall)
                {
                    pluginManager.getPlugin(id).uninstall();
                }
            }
            else
            {
                pluginManager.installAll(CollectionUtils.map(syncActions.toInstall, new Mapping<PluginInfo, URI>()
                {
                    public URI map(PluginInfo pluginInfo)
                    {
                        return repository.getPluginLocation(pluginInfo);
                    }
                }));
            }
        }

        return syncActions.isRebootRequired();
    }

    private SyncActions determineRequiredActions(List<PluginInfo> repositoryPlugins)
    {
        SyncActions actions = new SyncActions();
        for (PluginInfo pluginInfo: repositoryPlugins)
        {
            categoriseRepositoryPlugin(pluginInfo, actions);
        }

        for (final Plugin runningPlugin: CollectionUtils.filter(pluginManager.getPlugins(), new PluginRunningPredicate()))
        {
            if (!pluginInRepository(repositoryPlugins, runningPlugin))
            {
                actions.toUninstall.add(runningPlugin.getId());
            }
        }
        
        return actions;
    }

    private boolean pluginInRepository(List<PluginInfo> repositoryPlugins, final Plugin plugin)
    {
        return CollectionUtils.contains(repositoryPlugins, new Predicate<PluginInfo>()
        {
            public boolean satisfied(PluginInfo pluginInfo)
            {
                return pluginInfo.getId().equals(plugin.getId());
            }
        });
    }

    private void categoriseRepositoryPlugin(PluginInfo pluginInfo, SyncActions actions)
    {
        Plugin installedPlugin = pluginManager.getPlugin(pluginInfo.getId());
        if (installedPlugin == null)
        {
            actions.toInstall.add(pluginInfo);
        }
        else if (!pluginInfo.getVersion().equals(installedPlugin.getVersion().toString()))
        {
            actions.toUpgrade.add(pluginInfo);
        }
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }

    private class SyncActions
    {
        private List<PluginInfo> toInstall = new LinkedList<PluginInfo>();
        private List<PluginInfo> toUpgrade = new LinkedList<PluginInfo>();
        private List<String> toUninstall = new LinkedList<String>();
        
        public boolean isRebootRequired()
        {
            return toUpgrade.size() > 0 || toUninstall.size() > 0;
        }
    }
}
