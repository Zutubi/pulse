package com.cinnamonbob.upgrade;

import com.cinnamonbob.Version;

/**
 * <class-comment/>
 */
public class DefaultUpgradeContext implements UpgradeContext
{
    private int from;
    private int to;

    public DefaultUpgradeContext(Version from, Version to)
    {
        this.from = Integer.parseInt(from.getBuildNumber());
        this.to = Integer.parseInt(to.getBuildNumber());
    }

    public int getFromBuild()
    {
        return from;
    }

    public int getToBuild()
    {
        return to;
    }
}
