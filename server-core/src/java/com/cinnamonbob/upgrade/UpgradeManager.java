package com.cinnamonbob.upgrade;

import java.util.List;

/**
 * <class-comment/>
 */
public interface UpgradeManager
{

    /**
     * Returns true if the execution of upgrade tasks is required, false otherwise.
     *
     */
    boolean isUpgradeRequired();

    /**
     * Execute the required upgrade tasks.
     *
     * @return a list of executed upgrade tasks.
     */
    List<UpgradeTask> executeUpgrade();

    /**
     * Retrieve the list of upgrade tasks that need to be executed to satisfy the upgrade
     * requirements.
     *
     * @return a list of upgrade tasks.
     */
    List<UpgradeTask> previewUpgrade();
}
