package com.zutubi.pulse.upgrade;

import com.zutubi.pulse.bootstrap.Home;

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
    boolean isUpgradeRequired(Home home);

    void prepareUpgrade(Home home);

    List<UpgradeTask> previewUpgrade();

    /**
     * Execute the required upgrade tasks.
     *
     */
    void executeUpgrade();

    UpgradeProgressMonitor getUpgradeMonitor();
}
