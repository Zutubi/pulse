package com.cinnamonbob.upgrade;

import com.cinnamonbob.Version;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

    public void setTasks(List<UpgradeTask> tasks)
    {
        this.upgradeTasks = tasks;
    }

    public boolean isUpgradeRequired(Version fromVersion, Version toVersion)
    {
        if ( fromVersion.compareTo(toVersion) < 0 )
        {
            List<UpgradeTask> tasksToExecute = previewUpgrade(fromVersion, toVersion);
            if (tasksToExecute.size() > 0)
            {
                return true;
            }
        }
        return false;
    }

    public List<UpgradeTask> executeUpgrade(Version fromVersion, Version toVersion)
    {
        List<UpgradeTask> tasksToExecute = previewUpgrade(fromVersion, toVersion);
        List<UpgradeTask> tasksExecuted = new LinkedList<UpgradeTask>();

        UpgradeContext context = new DefaultUpgradeContext(fromVersion, toVersion);
        for (UpgradeTask task : tasksToExecute)
        {
            try
            {
                task.execute(context);
                tasksExecuted.add(task);
            }
            catch (UpgradeException e)
            {
                tasksExecuted.add(task);
                if (task.haltOnFailure())
                {
                    break;
                }
            }
        }
        return tasksExecuted;
    }

    public List<UpgradeTask> previewUpgrade(Version fromVersion, Version toVersion)
    {
        List<UpgradeTask> tasksToBeExecuted = new LinkedList<UpgradeTask>();
        int buildNumber = Integer.parseInt(fromVersion.getBuildNumber());

        for (UpgradeTask task : upgradeTasks)
        {
            if (buildNumber < task.getBuildNumber())
            {
                tasksToBeExecuted.add(task);
            }
        }
        Collections.sort(tasksToBeExecuted, new UpgradeTaskComparator());
        return tasksToBeExecuted;
    }
}
