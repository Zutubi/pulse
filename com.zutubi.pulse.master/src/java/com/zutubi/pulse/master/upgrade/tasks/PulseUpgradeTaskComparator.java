package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Comparator;

/**
 *
 *
 */
public class PulseUpgradeTaskComparator implements Comparator<PulseUpgradeTask>
{
    public int compare(PulseUpgradeTask a, PulseUpgradeTask b)
    {
        return a.getBuildNumber() - b.getBuildNumber();
    }
}
