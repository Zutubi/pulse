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

package com.zutubi.pulse.slave.command;

import com.zutubi.pulse.core.plugins.PluginException;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.plugins.repository.PluginRepository;
import com.zutubi.pulse.core.plugins.repository.http.HttpPluginRepository;
import com.zutubi.pulse.core.plugins.sync.PluginSynchroniser;
import com.zutubi.pulse.servercore.ShutdownManager;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import com.zutubi.pulse.servercore.services.MasterService;
import com.zutubi.pulse.servercore.services.UpgradeState;
import com.zutubi.pulse.servercore.services.UpgradeStatus;
import com.zutubi.pulse.slave.MasterProxyFactory;
import com.zutubi.util.Constants;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.jobs.IJobManager;

import java.net.MalformedURLException;

/**
 * A command used to run plugin synchronisation against a Pulse master.
 */
public class SyncPluginsCommand implements Runnable
{
    private static final Logger LOG = Logger.getLogger(SyncPluginsCommand.class);

    private static final long EXTENSIONS_TIMEOUT = 5 * Constants.MINUTE;
    
    private String master;
    private String token;
    private long hostId;
    private String pluginRepositoryUrl;
    
    private MasterProxyFactory masterProxyFactory;
    private PluginSynchroniser pluginSynchroniser;
    private JettyServerManager jettyServerManager;
    private ShutdownManager shutdownManager;
    private PluginManager pluginManager;

    public SyncPluginsCommand(String master, String token, long hostId, String pluginRepositoryUrl)
    {
        this.master = master;
        this.token = token;
        this.hostId = hostId;
        this.pluginRepositoryUrl = pluginRepositoryUrl;
    }

    public void run()
    {
        MasterService masterService;
        try
        {
            masterService = masterProxyFactory.createProxy(master);
        }
        catch (MalformedURLException e)
        {
            LOG.severe(e);
            return;
        }

        try
        {
            if (pluginSynchroniser.synchroniseWithRepository(new HttpPluginRepository(pluginRepositoryUrl), PluginRepository.Scope.SERVER))
            {
                jettyServerManager.stop(false);
                sendMessage(masterService, UpgradeState.REBOOTING);
                shutdownManager.reboot();
            }
            else
            {
                waitForExtensions();
                sendMessage(masterService, UpgradeState.COMPLETE);                
            }
        }
        catch (Exception e)
        {
            LOG.severe("Exception during plugin synchronisation: " + e.getMessage(), e);
            sendMessage(masterService, UpgradeState.ERROR, "Error during plugin synchronisation: " + e.getMessage());
        }
    }

    private void waitForExtensions() throws PluginException
    {
        long endTime = System.currentTimeMillis() + EXTENSIONS_TIMEOUT;
        IJobManager jobManager = pluginManager.getJobManager();
        while (!jobManager.isIdle())
        {
            if (System.currentTimeMillis() > endTime)
            {
                throw new PluginException("Timed out waiting for extensions");
            }
            try
            {
                Thread.sleep(Constants.SECOND);
            }
            catch (InterruptedException e)
            {
                throw new PluginException("Interrupted waiting for extension", e);
            }
        }
    }
    
    private void sendMessage(MasterService masterService, UpgradeState state)
    {
        sendMessage(masterService, state, null);
    }

    private void sendMessage(MasterService masterService, UpgradeState state, String message)
    {
        sendMessage(masterService, state, -1, message);
    }

    private void sendMessage(MasterService masterService, UpgradeState state, int progress, String message)
    {
        try
        {
            masterService.upgradeStatus(token, new UpgradeStatus(hostId, state, progress, message));
        }
        catch (Exception e)
        {
            LOG.severe("Error reporting upgrade status to master", e);
        }
    }

    public void setMasterProxyFactory(MasterProxyFactory masterProxyFactory)
    {
        this.masterProxyFactory = masterProxyFactory;
    }

    public void setPluginSynchroniser(PluginSynchroniser pluginSynchroniser)
    {
        this.pluginSynchroniser = pluginSynchroniser;
    }

    public void setShutdownManager(ShutdownManager shutdownManager)
    {
        this.shutdownManager = shutdownManager;
    }

    public void setJettyServerManager(JettyServerManager jettyServerManager)
    {
        this.jettyServerManager = jettyServerManager;
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
