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

package com.zutubi.pulse.dev.sync;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.plugins.PluginException;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.repository.PluginInfo;
import com.zutubi.pulse.core.plugins.repository.PluginRepository;
import com.zutubi.pulse.core.plugins.repository.http.HttpPluginRepository;
import com.zutubi.pulse.core.plugins.sync.PluginSynchroniser;
import com.zutubi.pulse.core.plugins.sync.SynchronisationActions;
import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.pulse.core.ui.api.YesNoResponse;
import com.zutubi.pulse.dev.client.AbstractClient;
import com.zutubi.pulse.dev.client.ClientException;
import com.zutubi.pulse.dev.config.DevConfig;
import com.zutubi.util.Constants;
import com.zutubi.util.StringUtils;
import org.eclipse.core.runtime.jobs.IJobManager;

import java.util.Collection;
import java.util.List;

/**
 * A dev client that runs a plugin synchroniser against a Pulse master's plugin
 * repository.
 */
public class SynchronisePluginsClient extends AbstractClient<DevConfig>
{
    private static final Messages I18N = Messages.getInstance(SynchronisePluginsClient.class);

    private PluginManager pluginManager;
    private PluginSynchroniser pluginSynchroniser;

    public SynchronisePluginsClient(DevConfig config, UserInterface ui)
    {
        super(config, ui);
    }

    /**
     * Runs a synchronise if the system has no plugins installed (and the user
     * agrees).  After this method returns the calling command should continue.
     * 
     * @throws ClientException on any error
     */
    public void syncIfBare() throws ClientException
    {
        if (pluginManager.getPlugins().size() == 0)
        {
            YesNoResponse response = ui.yesNoPrompt(I18N.format("no.plugins"), false, false, YesNoResponse.YES);
            if (response.isAffirmative())
            {
                syncPlugins();
            }
        }
    }
    
    /**
     * Synchronises the plugins with the Pulse master.
     * 
     * @return true if a reboot is required to complete the synchronisation,
     *         false if it is possible to just continue
     * @throws ClientException on any error
     */
    public boolean syncPlugins() throws ClientException
    {
        boolean rebootRequired = false;
        
        ensureServerConfigured();

        HttpPluginRepository repository = new HttpPluginRepository(StringUtils.join("/", true, config.getPulseUrl(), "pluginrepository/"));
        try
        {
            ui.status(I18N.format("retrieving"));
            Collection<PluginInfo> available = repository.getAvailablePlugins(PluginRepository.Scope.CORE);
            ui.status(I18N.format("retrieving.complete"));
            ui.status(I18N.format("determining.actions"));
            SynchronisationActions requiredActions = pluginSynchroniser.determineRequiredActions(available);
            showActions(ui, requiredActions);
            ui.status(I18N.format("determining.actions.complete"));
            if (requiredActions.isSyncRequired())
            {
                ui.status(I18N.format("synchronising"));
                rebootRequired = pluginSynchroniser.synchronise(repository, requiredActions);
                waitForExtensions();
                ui.status(I18N.format("synchronisation.complete"));
            }
            else
            {
                ui.status(I18N.format("plugins.up.to.date"));
            }
            
            return rebootRequired;
        }
        catch (Exception e)
        {
            throw new ClientException(e);
        }
    }

    private void showActions(UserInterface ui, SynchronisationActions actions)
    {
        ui.enterContext();

        List<PluginInfo> toInstall = actions.getToInstall();
        if (toInstall.size() > 0)
        {
            ui.status(I18N.format("to.install"));
            ui.enterContext();
            for (PluginInfo info: toInstall)
            {
                ui.status(info.getId() + ":" + info.getVersion());
            }
            ui.exitContext();
        }

        List<PluginInfo> toUpgrade = actions.getToUpgrade();
        if (toUpgrade.size() > 0)
        {
            ui.status(I18N.format("to.upgrade"));
            ui.enterContext();
            for (PluginInfo info: toUpgrade)
            {
                ui.status(info.getId() + ":" + info.getVersion());
            }
            ui.exitContext();
        }
        
        List<String> toUninstall = actions.getToUninstall();
        if (toUninstall.size() > 0)
        {
            ui.status(I18N.format("to.uninstall"));
            ui.enterContext();
            for (String id: toUninstall)
            {
                ui.status(id);
            }
            ui.exitContext();
        }

        ui.exitContext();
    }
    
    private void waitForExtensions() throws PluginException
    {
        IJobManager jobManager = pluginManager.getJobManager();
        while (!jobManager.isIdle())
        {
            try
            {
                Thread.sleep(Constants.SECOND);
            }
            catch (InterruptedException e)
            {
                throw new PluginException("Interrupted waiting for extensions", e);
            }
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
