package com.zutubi.pulse.master.agent;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.events.HostUpgradeCompleteEvent;
import com.zutubi.pulse.master.servlet.DownloadPackageServlet;
import com.zutubi.pulse.master.servlet.PluginRepositoryServlet;
import com.zutubi.pulse.servercore.services.UpgradeState;
import com.zutubi.pulse.servercore.services.UpgradeStatus;
import com.zutubi.util.Constants;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.concurrent.*;

/**
 * An active object (i.e. runs in it's own thread) that tries to update a
 * host.  Tracks the host progress through the update process, and tries
 * to detect failures and update the slave persistent status appropriately.
 */
public class HostUpdater implements Runnable
{
    private static final Logger LOG = Logger.getLogger(HostUpdater.class);

    private static final String PROPERTY_STATUS_TIMEOUT = "pulse.agent.upgrade.status.timeout";
    private static final String PROPERTY_REBOOT_TIMEOUT = "pulse.agent.upgrade.reboot.timeout";
    private static final String PROPERTY_REBOOT_PING_INTERVAL = "pulse.agent.upgrade.reboot.ping.interval";

    private DefaultHost host;
    private HostService hostService;
    private ExecutorService executor;
    private LinkedBlockingQueue<UpgradeStatus> statuses = new LinkedBlockingQueue<UpgradeStatus>();

    /**
     * Maximum number of seconds to wait between status events before timing
     * out the upgrade.
     */
    private long statusTimeout = Long.getLong(PROPERTY_STATUS_TIMEOUT, 600);

    /**
     * Maximum number of seconds to wait between receiving the reboot status and
     * a successful ping.
     */
    private long rebootTimeout = Long.getLong(PROPERTY_REBOOT_TIMEOUT, 600);

    /**
     * Number of seconds between pings while waiting for reboot.
     */
    private long pingInterval = Long.getLong(PROPERTY_REBOOT_PING_INTERVAL, 5);

    private MasterConfigurationManager configurationManager;
    private EventManager eventManager;
    private MasterLocationProvider masterLocationProvider;
    private ThreadFactory threadFactory;

    public HostUpdater(DefaultHost host, HostService hostService)
    {
        this.host = host;
        this.hostService = hostService;
    }

    public void start()
    {
        executor = Executors.newSingleThreadExecutor(threadFactory);
        executor.execute(this);
    }

    public void run()
    {
        try
        {
            int masterBuildNumber = Version.getVersion().getBuildNumberAsInt();
            int hostBuildNumber = hostService.ping();
            if (hostBuildNumber != masterBuildNumber)
            {
                if (!updateVersion())
                {
                    completed(false);
                    return;
                }
            }

            if (!syncPlugins())
            {
                completed(false);
                return;
            }

            host.upgradeStatus(UpgradeState.INITIAL, -1, null);
            completed(true);
        }
        catch (Exception e)
        {
            // Something went wrong
            LOG.warning(e);
            host.upgradeStatus(UpgradeState.ERROR, -1, e.getMessage());
            completed(false);
        }
    }

    private boolean updateVersion() throws InterruptedException
    {
        String masterUrl = masterLocationProvider.getMasterUrl();
        File packageFile = DownloadPackageServlet.getAgentZip(configurationManager.getSystemPaths());
        String packageUrl = DownloadPackageServlet.getPackagesUrl(masterUrl) + "/" + packageFile.getName();
        String masterBuild = Version.getVersion().getBuildNumber();

        if (hostService.updateVersion(masterBuild, masterUrl, host.getId(), packageUrl, packageFile.length()))
        {
            return monitorUpdate();
        }
        else
        {
            host.upgradeStatus(UpgradeState.FAILED, -1, "Host rejected upgrade, manual upgrade required.");
            return false;
        }
    }

    private boolean syncPlugins() throws InterruptedException
    {
        String masterUrl = masterLocationProvider.getMasterUrl();
        if (hostService.syncPlugins(masterUrl, host.getId(), masterUrl + "/" + PluginRepositoryServlet.PATH_REPOSITORY))
        {
            host.upgradeStatus(UpgradeState.SYNCHRONISING_PLUGINS, -1, null);
            return monitorUpdate();
        }
        else
        {
            return true;
        }
    }

    private boolean monitorUpdate() throws InterruptedException
    {
        boolean rebooting = false;
        while (!rebooting)
        {
            UpgradeStatus status = statuses.poll(statusTimeout, TimeUnit.SECONDS);
            if (status == null)
            {
                host.upgradeStatus(UpgradeState.FAILED, -1, "Timed out waiting for message from host.");
                return false;
            }

            host.upgradeStatus(status.getState(), status.getProgress(), status.getMessage());
            switch (status.getState())
            {
                case COMPLETE:
                    return true;
                case ERROR:
                case FAILED:
                    return false;
                case REBOOTING:
                    rebooting = true;
                    break;
            }
        }

        // Now the agent is rebooting, ping it until it is back up.
        long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(rebootTimeout);
        int foundBuild = 0;
        int expectedBuild = Version.getVersion().getBuildNumberAsInt();
        while (System.currentTimeMillis() < endTime)
        {
            try
            {
                foundBuild = hostService.ping();
                if (foundBuild == expectedBuild)
                {
                    return true;
                }
            }
            catch (Exception e)
            {
                // We expect some pings to fail, so can't read too much into it
                Thread.sleep(pingInterval * Constants.SECOND);
            }
        }

        if (foundBuild != 0)
        {
            host.upgradeStatus(UpgradeState.FAILED, -1, "Host failed to upgrade to expected build.  Expected build " + expectedBuild + " but found " + foundBuild);
            return false;
        }
        else
        {
            host.upgradeStatus(UpgradeState.FAILED, -1, "Timed out waiting for host to reboot.");
            return false;
        }
    }

    private void completed(boolean succeeded)
    {
        eventManager.publish(new HostUpgradeCompleteEvent(this, host, succeeded));
    }

    public void stop(boolean force)
    {
        if (executor != null)
        {
            if (force)
            {
                executor.shutdownNow();
            }
            else
            {
                executor.shutdown();
            }
        }
    }

    /**
     * Notify the host updater of a upgrade status event.
     *
     * @param upgradeStatus the new upgrade status from the agent being upgraded.
     */
    public void upgradeStatus(UpgradeStatus upgradeStatus)
    {
        // only accept upgrade status events from the host we are dealing with.
        if (host.getId() != upgradeStatus.getHandle())
        {
            throw new IllegalArgumentException("Upgrade status received from incorrect host.");
        }
        statuses.add(upgradeStatus);
    }

    /**
     * The time in seconds that the updater will wait between receiving status updates
     * from the agent.
     *
     * @param seconds time in seconds
     */
    public void setStatusTimeout(long seconds)
    {
        this.statusTimeout = seconds;
    }

    /**
     * The time in seconds that the updater will wait for the remote agent to reboot.
     *
     * @param seconds time in seconds
     */
    public void setRebootTimeout(long seconds)
    {
        this.rebootTimeout = seconds;
    }

    /**
     * The approximate interval in seconds between successive pings.
     *
     * @param seconds time in seconds
     */
    public void setPingInterval(long seconds)
    {
        this.pingInterval = seconds;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    public void setMasterLocationProvider(MasterLocationProvider masterLocationProvider)
    {
        this.masterLocationProvider = masterLocationProvider;
    }
}
