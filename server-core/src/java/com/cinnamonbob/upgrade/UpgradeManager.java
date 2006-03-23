package com.cinnamonbob.upgrade;

import com.cinnamonbob.Version;

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
    boolean isUpgradeRequired(Version fromVersion, Version toVersion);

    /**
     * Execute the required upgrade tasks.
     *
     * @return a list of executed upgrade tasks.
     */
    List<UpgradeTask> executeUpgrade(Version fromVersion, Version toVersion);

    /**
     * Retrieve the list of upgrade tasks that need to be executed to satisfy the upgrade
     * requirements.
     *
     * @return a list of upgrade tasks.
     */
    List<UpgradeTask> previewUpgrade(Version fromVersion, Version toVersion);
}
