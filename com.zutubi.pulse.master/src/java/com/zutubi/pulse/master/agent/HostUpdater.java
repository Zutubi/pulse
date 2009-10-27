package com.zutubi.pulse.master.agent;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.events.HostUpgradeCompleteEvent;
import com.zutubi.pulse.master.servlet.DownloadPackageServlet;
import com.zutubi.pulse.servercore.services.UpgradeState;
import com.zutubi.pulse.servercore.services.UpgradeStatus;
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
    
    private DefaultHost host;
    private HostService hostService;
    private ExecutorService executor;
    private LinkedBlockingQueue<UpgradeStatus> statuses = new LinkedBlockingQueue<UpgradeStatus>();

    /**
     * Maximum number of seconds to wait between status events before timing
     * out the upgrade.
     */
    private long statusTimeout = 600;

    /**
     * Maximum number of seconds to wait between receiving the reboot status and
     * a successful ping.
     */
    private long rebootTimeout = 300;

    /**
     * Number of milliseconds between pings while waiting for reboot.
     */
    private long pingInterval = 5000;

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
        String masterUrl = masterLocationProvider.getMasterUrl();
        File packageFile = DownloadPackageServlet.getAgentZip(configurationManager.getSystemPaths());
        String packageUrl = DownloadPackageServlet.getPackagesUrl(masterUrl) + "/" + packageFile.getName();
        String masterBuild = Version.getVersion().getBuildNumber();

        try
        {
            boolean accepted = hostService.updateVersion(masterBuild, masterUrl, host.getId(), packageUrl, packageFile.length());

            if(!accepted)
            {
                host.upgradeStatus(UpgradeState.FAILED, -1, "Host rejected upgrade, manual upgrade required.");
                completed(false);
                return;
            }

            boolean rebooting = false;
            while (!rebooting)
            {
                UpgradeStatus status = statuses.poll(statusTimeout, TimeUnit.SECONDS);
                if(status == null)
                {
                    host.upgradeStatus(UpgradeState.FAILED, -1, "Timed out waiting for message from host.");
                    completed(false);
                    return;
                }

                host.upgradeStatus(status.getState(), status.getProgress(), status.getMessage());
                switch(status.getState())
                {
                    case ERROR:
                    case FAILED:
                        completed(false);
                        return;
                    case REBOOTING:
                        rebooting = true;
                        break;
                }
            }

            // Now the agent is rebooting, ping it until it is back up.
            long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(rebootTimeout);
            int expectedBuild = Version.getVersion().getBuildNumberAsInt();
            int foundBuild = 0;
            while(System.currentTimeMillis() < endTime)
            {
                try
                {
                    foundBuild = hostService.ping();
                    if(foundBuild == expectedBuild)
                    {
                        // We did it!
                        host.upgradeStatus(UpgradeState.INITIAL, -1, null);
                        completed(true);
                        return;
                    }
                }
                catch(Exception e)
                {
                    // We expect some pings to fail, so can't read too much into it
                    Thread.sleep(pingInterval);
                }
            }

            if (foundBuild != 0)
            {
                // ping returned but the build was not the expected build.
                host.upgradeStatus(UpgradeState.FAILED, -1, "Host failed to upgrade to expected build.  Expected build " + expectedBuild + " but found " + foundBuild);
                completed(false);
            }
            else
            {
                host.upgradeStatus(UpgradeState.FAILED, -1, "Timed out waiting for host to reboot.");
                completed(false);
            }
        }
        catch (Exception e)
        {
            // Something went wrong
            LOG.warning(e);
            host.upgradeStatus(UpgradeState.ERROR, -1, e.getMessage());
            completed(false);
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
            if(force)
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
     * @param upgradeStatus     the new upgrade status from the agent being upgraded.
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
     * @param seconds   time in seconds
     */
    public void setStatusTimeout(long seconds)
    {
        this.statusTimeout = seconds;
    }

    /**
     * The time in seconds that the updater will wait for the remote agent to reboot.
     *
     * @param seconds   time in seconds
     */
    public void setRebootTimeout(long seconds)
    {
        this.rebootTimeout = seconds;
    }

    /**
     * The approximate interval in milliseconds between successive pings.
     *
     * @param milliseconds  time in milliseconds
     */
    public void setPingInterval(long milliseconds)
    {
        this.pingInterval = milliseconds;
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
