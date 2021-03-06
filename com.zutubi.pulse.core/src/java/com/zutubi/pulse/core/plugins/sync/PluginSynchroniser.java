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

package com.zutubi.pulse.core.plugins.sync;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginException;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.PluginRunningPredicate;
import com.zutubi.pulse.core.plugins.repository.PluginInfo;
import com.zutubi.pulse.core.plugins.repository.PluginRepository;

import java.net.URI;
import java.util.Collection;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

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
                pluginManager.installAll(newArrayList(transform(syncActions.getToInstall(), new Function<PluginInfo, URI>()
                {
                    public URI apply(PluginInfo pluginInfo)
                    {
                        return repository.getPluginLocation(pluginInfo);
                    }
                })));
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
