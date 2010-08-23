package com.zutubi.pulse.slave.command;

import com.zutubi.pulse.core.plugins.repository.PluginRepository;
import com.zutubi.pulse.core.plugins.repository.http.HttpPluginRepository;
import com.zutubi.pulse.core.plugins.sync.PluginSynchroniser;
import com.zutubi.pulse.servercore.ShutdownManager;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;
import com.zutubi.pulse.servercore.services.MasterService;
import com.zutubi.pulse.servercore.services.UpgradeState;
import com.zutubi.pulse.servercore.services.UpgradeStatus;
import com.zutubi.pulse.slave.MasterProxyFactory;
import com.zutubi.util.logging.Logger;

import java.net.MalformedURLException;

/**
 * A command used to run plugin synchronisation against a Pulse master.
 */
public class SyncPluginsCommand implements Runnable
{
    private static final Logger LOG = Logger.getLogger(SyncPluginsCommand.class);

    private String master;
    private String token;
    private long hostId;
    private String pluginRepositoryUrl;
    
    private MasterProxyFactory masterProxyFactory;
    private PluginSynchroniser pluginSynchroniser;
    private JettyServerManager jettyServerManager;
    private ShutdownManager shutdownManager;

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
                System.out.println("Sync command: send complete");
                sendMessage(masterService, UpgradeState.COMPLETE);
            }
        }
        catch (Exception e)
        {
            LOG.severe("Exception during plugin synchronisation: " + e.getMessage(), e);
            sendMessage(masterService, UpgradeState.ERROR, "Error during plugin synchronisation: " + e.getMessage());
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
}
