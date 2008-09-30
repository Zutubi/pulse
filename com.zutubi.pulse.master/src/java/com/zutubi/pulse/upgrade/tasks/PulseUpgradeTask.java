package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeTask;

/**
 *
 *
 */
public interface PulseUpgradeTask extends UpgradeTask
{
    /**
     * Return the build number that will trigger this upgrade task.
     *
     * For example: If the setting this to 301 will cause this task to
     * be executed on all installations with build version 300 and below (inclusive).
     *
     * @return the build number that triggers this upgrade task
     */
    int getBuildNumber();
}