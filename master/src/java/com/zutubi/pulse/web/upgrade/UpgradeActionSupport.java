package com.zutubi.pulse.web.upgrade;

import com.zutubi.pulse.upgrade.UpgradeManager;
import com.zutubi.pulse.web.ActionSupport;

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
