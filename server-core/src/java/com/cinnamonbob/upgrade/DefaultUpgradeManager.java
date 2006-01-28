package com.cinnamonbob.upgrade;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

/**
 * <class-comment/>
 */
public class DefaultUpgradeManager implements UpgradeManager
{
    private List<UpgradeTask> upgradeTasks = new LinkedList<UpgradeTask>();

    public void addTask(UpgradeTask task)
    {
        upgradeTasks.add(task);
    }

    public boolean isUpgradeRequired()
    {
        return upgradeTasks.size() > 0;
    }

    public List<UpgradeTask> executeUpgrade()
    {
        List<UpgradeTask> executedTasks = previewUpgrade();
        upgradeTasks.clear();
        return executedTasks;
    }

    public List<UpgradeTask> previewUpgrade()
    {
        List<UpgradeTask> tasksToBeExecuted = new LinkedList<UpgradeTask>();
        tasksToBeExecuted.addAll(upgradeTasks);
        Collections.sort(tasksToBeExecuted, new UpgradeTaskComparator());
        return tasksToBeExecuted;
    }
}
