package com.zutubi.pulse.master.upgrade;

import com.zutubi.pulse.master.util.monitor.Monitor;

import java.util.List;

/**
 * The upgrade manager is responsible for handling the upgrade process during the
 * Pulse server startup.
 *
 * The workflow for the upgrade manager is as follows.
 * <ul>
 * <li>An upgrade is required if and only if {@link #isUpgradeRequired()} returns true.</li>
 * <li>If an upgrade is required, the first step is to {@link #prepareUpgrade()} the upgrade.</li>
 * <li>Once the upgrade has been prepared, it can be previewed via ({@link #previewUpgrade()} and monitored
 * via ({@link #getMonitor()}.</li>
 * <li>When the upgrade has been prepared, and only then, you can run the upgrade via {@link #executeUpgrade()}</li>
 * </ul>
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
     * @return the list of upgrade task groups that make up the prepared upgrade.
     */
    List<UpgradeTaskGroup> prepareUpgrade();

    /**
     * Preview the prepared upgrade.  This returns the same list as returned by the prepareUpgrade call.
     *
     * @return a list of upgrade tasks that will be executed if execute upgrade is called.
     *
     * @see #executeUpgrade()
     */
    List<UpgradeTaskGroup> previewUpgrade();

    /**
     * Execute the required upgrade tasks.
     */
    void executeUpgrade();

    /**
     * Get the progress monitor for the upgrade.
     *
     * @return a progress monitor for the currenly active upgrade, or null if no upgrade is in progress.
     */
    Monitor getMonitor();
}
