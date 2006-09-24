package com.zutubi.pulse.agent;

import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.services.UpgradeStatus;
import com.zutubi.pulse.services.UpgradeState;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.SlaveUpgradeCompleteEvent;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.servlet.DownloadPackageServlet;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.io.File;

/**
 * An active object (i.e. runs in it's own thread) that tries to update an
 * agent.  Tracks the agent progress through the update process, and tries
 * to detect failures and update the slave persistent status appropriately.
 */
public class AgentUpdater implements Runnable
{
    private static final Logger LOG = Logger.getLogger(AgentUpdater.class);
    
    private SlaveAgent agent;
    private String token;
    private String masterUrl;
    private EventManager eventManager;
    private SystemPaths systemPaths;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private LinkedBlockingQueue<UpgradeStatus> statuses = new LinkedBlockingQueue<UpgradeStatus>();

    public AgentUpdater(SlaveAgent agent, String token, String masterUrl, EventManager eventManager, SystemPaths systemPaths)
    {
        this.agent = agent;
        this.token = token;
        this.masterUrl = masterUrl;
        this.eventManager = eventManager;
        this.systemPaths = systemPaths;
    }

    public void start()
    {
        executor.execute(this);
    }

    public void run()
    {
        SlaveService slaveService = agent.getSlaveService();
        File packageFile = DownloadPackageServlet.getAgentZip(systemPaths);
        String packageUrl = DownloadPackageServlet.getPackagesUrl(masterUrl) + "/" + packageFile.getName();
        String masterBuild = Version.getVersion().getBuildNumber();

        try
        {
            boolean accepted = slaveService.updateVersion(token, masterBuild, masterUrl, agent.getSlave().getId(), packageUrl, packageFile.length());

            if(!accepted)
            {
                agent.upgradeStatus(UpgradeState.FAILED, -1, "Agent rejected upgrade, manual upgrade required.");
                completed(false);
                return;
            }

            boolean rebooting = false;
            while (!rebooting)
            {
                UpgradeStatus status = statuses.poll(600, TimeUnit.SECONDS);
                if(status == null)
                {
                    agent.upgradeStatus(UpgradeState.FAILED, -1, "Timed out waiting for message from agent.");
                    completed(false);
                    return;
                }

                agent.upgradeStatus(status.getState(), status.getProgress(), status.getMessage());
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
            long endTime = System.currentTimeMillis() + 300000;
            int expectedBuild = Version.getVersion().getBuildNumberAsInt();
            while(System.currentTimeMillis() < endTime)
            {
                try
                {
                    int build = slaveService.ping();
                    if(build == expectedBuild)
                    {
                        // We did it!
                        completed(true);
                        return;
                    }
                }
                catch(Exception e)
                {
                    // We expect some pings to fail, so can't read too much into it
                    Thread.sleep(5000);
                }
            }

            agent.upgradeStatus(UpgradeState.FAILED, -1, "Timed out waiting for agent to reboot.");
            completed(false);
        }
        catch (Exception e)
        {
            // Something went wrong
            LOG.warning(e);
            agent.upgradeStatus(UpgradeState.ERROR, -1, e.getMessage());
            completed(false);
        }
    }

    private void completed(boolean succeeded)
    {
        eventManager.publish(new SlaveUpgradeCompleteEvent(this, agent, succeeded));
    }

    public void stop(boolean force)
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

    public void upgradeStatus(UpgradeStatus upgradeStatus)
    {
        statuses.add(upgradeStatus);
    }
}
