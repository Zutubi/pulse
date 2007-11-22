package com.zutubi.pulse.upgrade;

import java.util.List;

/**
 * An upgrade component represents part of the system that can be upgraded.
 *
 * Each upgradeable component is asked whether or not an upgrade is required.  If so,
 * it receives a callback to the prepare upgrade method, followed later by a request for
 * the upgrade tasks that need to be executed for this upgrade.  These upgrade tasks are
 * in turn executed.
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
     * The list of configured upgrade tasks that need to be executed in order to
     * carry out the upgrade.
     *
     * @return a list of upgrade tasks, or an empty list if no upgrades are required.
     */
    List<UpgradeTask> getUpgradeTasks();

    void upgradeStarted();

    void upgradeCompleted();

    void upgradeAborted();
}
