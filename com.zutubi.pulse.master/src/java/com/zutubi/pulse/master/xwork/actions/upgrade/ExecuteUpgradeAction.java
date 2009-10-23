package com.zutubi.pulse.master.xwork.actions.upgrade;

import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.pulse.master.upgrade.UpgradeTaskGroup;

import java.util.List;

/**
 * <class-comment/>
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
        // check if the upgrade is in progress... if a user refreshes the browser on the
        // execute action, we do not want the upgrade starting again.
        Monitor progress = getMonitor();

        if (!progress.isStarted())
        {
            upgradeManager.executeUpgrade();
        }
        
        // CIB-1028: Initial upgrade page status shows 100%
        // Wait for the upgrade process to start before we return. Why? To prevent the possibility
        // of the upgrade status page being rendered BEFORE the upgrade system is properly initialised.
        while (!progress.isStarted())
        {
            Thread.yield();
        }

        // go to the progress monitor screen.
        return SUCCESS;
    }
}
