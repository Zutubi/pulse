package com.cinnamonbob.web.upgrade;

import com.cinnamonbob.upgrade.UpgradeManager;
import com.cinnamonbob.web.ActionSupport;

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
