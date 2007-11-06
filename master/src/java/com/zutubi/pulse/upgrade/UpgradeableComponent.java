package com.zutubi.pulse.upgrade;

import java.util.List;

/**
 *
 *
 */
public interface UpgradeableComponent
{
    boolean isUpgradeRequired();

    List<UpgradeTask> getUpgradeTasks();

    void prepareUpgrade();

    void completeUpgrade();
}
