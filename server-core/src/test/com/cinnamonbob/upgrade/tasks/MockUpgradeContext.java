package com.cinnamonbob.upgrade.tasks;

import com.cinnamonbob.upgrade.UpgradeContext;

/**
 * <class-comment/>
 */
public class MockUpgradeContext implements UpgradeContext
{
    private int fromBuild;
    private int toBuild;

    public int getFromBuild()
    {
        return fromBuild;
    }

    public void setFromBuild(int fromBuild)
    {
        this.fromBuild = fromBuild;
    }

    public int getToBuild()
    {
        return toBuild;
    }

    public void setToBuild(int toBuild)
    {
        this.toBuild = toBuild;
    }
}
