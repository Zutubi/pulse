package com.zutubi.pulse.master.xwork.actions.upgrade;

import com.zutubi.pulse.master.upgrade.UpgradeManager;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * <class-comment/>
 */
public class UpgradeActionSupport extends ActionSupport
{
    protected UpgradeManager upgradeManager;

    /**
     * Required resource.
     *
     * @param upgradeManager
     */
    public void setUpgradeManager(UpgradeManager upgradeManager)
    {
        this.upgradeManager = upgradeManager;
    }
}
