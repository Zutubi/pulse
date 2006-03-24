package com.cinnamonbob.web.upgrade;

import com.cinnamonbob.upgrade.UpgradeProgressMonitor;

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
        upgradeManager.executeUpgrade();
        return SUCCESS;
    }
}
