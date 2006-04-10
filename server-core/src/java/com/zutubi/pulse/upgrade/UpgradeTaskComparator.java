package com.cinnamonbob.upgrade;

import java.util.Comparator;

/**
 * <class-comment/>
 */
public class UpgradeTaskComparator implements Comparator
{
    public int compare(Object o1, Object o2)
    {
        UpgradeTask a = (UpgradeTask) o1;
        UpgradeTask b = (UpgradeTask) o2;
        return a.getBuildNumber() - b.getBuildNumber();
    }
}
