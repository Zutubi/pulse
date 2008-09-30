package com.zutubi.pulse.upgrade;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class UpgradeTaskGroup
{
    private UpgradeableComponent source;

    private List<UpgradeTask> tasks = new LinkedList<UpgradeTask>();

    public UpgradeableComponent getSource()
    {
        return source;
    }

    public void setSource(UpgradeableComponent source)
    {
        this.source = source;
    }

    public List<UpgradeTask> getTasks()
    {
        return tasks;
    }

    public void setTasks(List<UpgradeTask> tasks)
    {
        this.tasks = tasks;
    }
}
