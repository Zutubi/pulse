package com.zutubi.pulse.master.xwork.actions.upgrade;

import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.pulse.master.upgrade.UpgradeTaskGroup;
import com.zutubi.util.Constants;

import java.util.List;

/**
 * Action that starts and monitors the upgrade process.
 */
public class ExecuteUpgradeAction extends UpgradeActionSupport
{
    public Monitor getMonitor()
    {
        return upgradeManager.getMonitor();
    }

    public List<UpgradeTaskGroup> getOrderedTaskGroups()
    {
        return upgradeManager.previewUpgrade();
    }

    public String execute()
    {
        runOnce(new Runnable()
        {
            public void run()
            {
                upgradeManager.executeUpgrade();
            }
        }, getClass().getName());

        // CIB-1028: Initial upgrade page status shows 100%
        // Wait for the upgrade process to start before we return. Why? To prevent the possibility
        // of the upgrade status page being rendered BEFORE the upgrade system is properly initialised.
        Monitor progress = getMonitor();
        while (!progress.isStarted())
        {
            try
            {
                Thread.sleep(Constants.SECOND);
            }
            catch (InterruptedException e)
            {
                // noop.
            }
        }

        return SUCCESS;
    }
}
