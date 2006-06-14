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
     */
    boolean isUpgradeRequired(Data data);

    void prepareUpgrade(Data data);

    List<UpgradeTask> previewUpgrade();

    /**
     * Execute the required upgrade tasks.
     *
     */
    void executeUpgrade();

    UpgradeProgressMonitor getUpgradeMonitor();
}
