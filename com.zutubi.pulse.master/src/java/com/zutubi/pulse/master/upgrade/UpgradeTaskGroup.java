package com.zutubi.pulse.master.upgrade;

import java.util.LinkedList;
import java.util.List;

/**
 * A holder for a group of upgrade tasks some a single source upgradeable component
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
