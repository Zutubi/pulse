package com.zutubi.pulse.upgrade;

/**
 *
 *
 */
public interface UpgradeListener
{
    /**
     * This callback is triggered when the upgrade task has completed successfully.
     *
     * @param task
     */
    void taskCompleted(UpgradeTask task);

    /**
     * This callback is triggered when the upgrade task has failed.
     *
     * @param task
     */
    void taskFailed(UpgradeTask task);

    /**
     * This callback is triggered when the upgrade task was aborted.
     *
     * @param task
     */
    void taskAborted(UpgradeTask task);
}
