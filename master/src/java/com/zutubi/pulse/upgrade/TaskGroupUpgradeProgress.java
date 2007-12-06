package com.zutubi.pulse.upgrade;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class TaskGroupUpgradeProgress implements UpgradeProgress
{
    private List<TaskUpgradeProgress> tasks = new LinkedList<TaskUpgradeProgress>();

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

    public void add(TaskUpgradeProgress taskProgress)
    {
        tasks.add(taskProgress);
    }

    public List<TaskUpgradeProgress> getTasks()
    {
        return tasks;
    }

    public UpgradeTaskGroup getGroup()
    {
        return group;
    }
}
