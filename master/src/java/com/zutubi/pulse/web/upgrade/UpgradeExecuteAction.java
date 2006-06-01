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
        upgradeManager.executeUpgrade();
        return SUCCESS;
    }
}
