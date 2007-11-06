package com.zutubi.pulse.upgrade;

import java.util.List;

/**
 * <class-comment/>
 */
public interface UpgradeManager
{
    /**
     * Returns true if the execution of upgrade tasks is required, false otherwise.
     *
     * @return true if an upgrade is required before the specified data directory can be used, false otherwise.
     */
    boolean isUpgradeRequired();

    /**
     * Prepare upgrade is the first step for the upgrade processing.
     *
     */
    void prepareUpgrade();

    /**
     * Preview the prepared upgrade.
     *
     * @return a list of upgrade tasks that will be executed if execute upgrade is called.
     *
     * @see #executeUpgrade()
     */
    List<UpgradeTaskGroup> previewUpgrade();

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
