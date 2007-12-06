package com.zutubi.pulse.upgrade;

/**
 *
 *
 */
public class TaskUpgradeProgress
{
    private UpgradeTask task;

    /**
     * The current status of the task.
     */
    private UpgradeStatus status = UpgradeStatus.PENDING;

    public TaskUpgradeProgress(UpgradeTask task)
    {
        this.task = task;
    }

    public UpgradeStatus getStatus()
    {
        return status;
    }

    public void setStatus(UpgradeStatus status)
    {
        this.status = status;
    }

    public String getName()
    {
        return task.getName();
    }

    public String getMessage()
    {
        return task.getErrors().get(0);
    }
}
