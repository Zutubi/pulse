package com.zutubi.pulse.master.upgrade;

import java.util.List;

/**
 * An upgrade component represents part of the system that can be upgraded.
 *
 * Each upgradeable component is asked whether or not an upgrade is required.  
 */
public interface UpgradeableComponent
{
    /**
     * Indicates whether or not this upgradeable component requires an upgrade.
     *
     * @return true if an upgrade is required, false otherwise.
     */
    boolean isUpgradeRequired();

    /**
     * Indicates whether or not this upgradeable component requires an upgrade to move between the given builds.
     *
     * @param fromBuildNumber the original build number
     * @param toBuildNumber the target build number
     * @return true if an upgrade is required, false otherwise.
     */
    boolean isUpgradeRequired(int fromBuildNumber, int toBuildNumber);

    /**
     * The list of configured upgrade tasks that need to be executed in order to
     * carry out the upgrade.
     *
     * @return a list of upgrade tasks, or an empty list if no upgrades are required.
     */
    List<UpgradeTask> getUpgradeTasks();

    /**
     * Callback triggered at the start of the execution of the upgrade tasks associated with this component.
     */
    void upgradeStarted();

    /**
     * Callback triggered when the execution of the upgrade tasks associated with this component are completed.
     */
    void upgradeCompleted();

    /**
     * Callback triggered when the execution of the upgrade tasks associated with this component are aborted.
     */
    void upgradeAborted();
}
