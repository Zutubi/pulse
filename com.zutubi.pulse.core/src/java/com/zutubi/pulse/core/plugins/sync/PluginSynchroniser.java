package com.zutubi.pulse.core.plugins.sync;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import static com.google.common.collect.Iterables.any;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginException;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.PluginRunningPredicate;
import com.zutubi.pulse.core.plugins.repository.PluginInfo;
import com.zutubi.pulse.core.plugins.repository.PluginRepository;
import com.zutubi.util.CollectionUtils;

import java.net.URI;
import java.util.Collection;

/**
 * A service to synchronise plugins with a repository.
 */
public class PluginSynchroniser
{
    private PluginManager pluginManager;

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
        Collection<PluginInfo> repositoryPlugins = repository.getAvailablePlugins(scope);
        synchronized (pluginManager)
        {
            SynchronisationActions syncActions = determineRequiredActions(repositoryPlugins);
            return synchronise(repository, syncActions);
        }
    }

    /**
     * Returns the actions that would be required to synchronise with the given
     * set of plugins.
     *
     * @param repositoryPlugins the plugins to synchronise with
     * @return the collection of actions required to synchronise
     */
    public SynchronisationActions determineRequiredActions(Iterable<PluginInfo> repositoryPlugins)
    {
        SynchronisationActions actions = new SynchronisationActions();
        for (PluginInfo pluginInfo : repositoryPlugins)
        {
            categoriseRepositoryPlugin(pluginInfo, actions);
        }

        for (final Plugin runningPlugin : Iterables.filter(pluginManager.getPlugins(), new PluginRunningPredicate()))
        {
            if (!pluginInList(repositoryPlugins, runningPlugin))
            {
                actions.addUninstall(runningPlugin.getId());
            }
        }

        return actions;
    }

    /**
     * Runs the given actions using the given plugin repository to synchronise.
     * 
     * @param repository  the repository to retrieve plugins from
     * @param syncActions actions required to synchronise
     * @return true if a reboot is required to complete synchronisation, false
     *         if it was able to complete without a reboot
     * @throws PluginException on any error retrieving or working with a plugin
     */
    public boolean synchronise(final PluginRepository repository, SynchronisationActions syncActions) throws PluginException
    {
        synchronized (pluginManager)
        {
            if (syncActions.isRebootRequired())
            {
                for (PluginInfo pluginInfo : syncActions.getToInstall())
                {
                    pluginManager.requestInstall(repository.getPluginLocation(pluginInfo));
                }

                for (PluginInfo pluginInfo : syncActions.getToUpgrade())
                {
                    pluginManager.getPlugin(pluginInfo.getId()).upgrade(repository.getPluginLocation(pluginInfo));
                }

                for (String id : syncActions.getToUninstall())
                {
                    pluginManager.getPlugin(id).uninstall();
                }
            }
            else
            {
                pluginManager.installAll(CollectionUtils.map(syncActions.getToInstall(), new Function<PluginInfo, URI>()
                {
                    public URI apply(PluginInfo pluginInfo)
                    {
                        return repository.getPluginLocation(pluginInfo);
                    }
                }));
            }

            return syncActions.isRebootRequired();
        }
    }

    private boolean pluginInList(Iterable<PluginInfo> repositoryPlugins, final Plugin plugin)
    {
        return any(repositoryPlugins, new Predicate<PluginInfo>()
        {
            public boolean apply(PluginInfo pluginInfo)
            {
                return pluginInfo.getId().equals(plugin.getId());
            }
        });
    }

    private void categoriseRepositoryPlugin(PluginInfo pluginInfo, SynchronisationActions actions)
    {
        Plugin installedPlugin = pluginManager.getPlugin(pluginInfo.getId());
        if (installedPlugin == null)
        {
            actions.addInstall(pluginInfo);
        }
        else if (!pluginInfo.getVersion().equals(installedPlugin.getVersion().toString()))
        {
            actions.addUpgrade(pluginInfo);
        }
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
