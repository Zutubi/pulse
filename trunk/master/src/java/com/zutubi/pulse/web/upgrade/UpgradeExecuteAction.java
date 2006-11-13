package com.zutubi.pulse.web.upgrade;

import com.zutubi.pulse.upgrade.UpgradeProgressMonitor;

/**
 * <class-comment/>
 */
public class UpgradeExecuteAction extends UpgradeActionSupport
{
    public UpgradeProgressMonitor getMonitor()
    {
        return upgradeManager.getUpgradeMonitor();
    }

    public String execute()
    {
        // check if the upgrade is in progress... if a user refreshes the browser on the
        // execute action, we do not want the upgrade starting again.
        UpgradeProgressMonitor progress = getMonitor();

        if (!progress.isStarted())
        {
            upgradeManager.executeUpgrade();
        }

        // go to the progress monitor screen.
        return SUCCESS;
    }
}
