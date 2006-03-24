package com.cinnamonbob.upgrade;

import com.cinnamonbob.Version;

import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultUpgradeContext implements UpgradeContext
{
    private int from;
    private int to;

    private List<UpgradeTask> tasks = null;

    public DefaultUpgradeContext(Version from, Version to)
    {
        this.from = Integer.parseInt(from.getBuildNumber());
        this.to = Integer.parseInt(to.getBuildNumber());
    }

    public int getFromBuild()
    {
        return from;
    }

    public int getToBuild()
    {
        return to;
    }

    public void setTasks(List<UpgradeTask> tasks)
    {
        this.tasks = tasks;
    }

    public List<UpgradeTask> getTasks()
    {
        return tasks;
    }
}
