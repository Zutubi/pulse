package com.zutubi.pulse.upgrade;

import com.zutubi.pulse.bootstrap.Data;

import java.util.List;

/**
 * <class-comment/>
 */
public interface UpgradeManager
{
    /**
     * Returns true if the execution of upgrade tasks is required, false otherwise.
     *
     * @param data represents the data directory being checked.
     *
     * @return true if an upgrade is required before the specified data directory can be used, false otherwise.
     */
    boolean isUpgradeRequired(Data data);

    /**
     * Prepare upgrade is the first step for the upgrade processing.
     *
     * @param data the data instance to be upgraded.
     */
    void prepareUpgrade(Data data);

    /**
     * Preview the prepared upgrade.
     *
     * @return a list of upgrade tasks that will be executed if execute upgrade is called.
     *
     * @see #executeUpgrade()
     * @see #prepareUpgrade(com.zutubi.pulse.bootstrap.Data) 
     */
    List<UpgradeTask> previewUpgrade();

    /**
     * Execute the required upgrade tasks.
     *
     */
    void executeUpgrade();

    /**
     * Get the progress monitor for the upgrade.
     *
     * @return a progress monitor for the currenly active upgrade, or null if no upgrade is in progress.
     */
    UpgradeProgressMonitor getUpgradeMonitor();
}
