package com.zutubi.pulse.upgrade;

/**
 *
 *
 */
public class TaskGroupUpgradeProgress implements UpgradeProgress
{
    private UpgradeTaskGroup group;

    private UpgradeStatus status;

    public TaskGroupUpgradeProgress(UpgradeTaskGroup group)
    {
        this.group = group;
    }

    public UpgradeStatus getStatus()
    {
        return status;
    }

    public void setStatus(UpgradeStatus status)
    {
        this.status = status;
    }
}
