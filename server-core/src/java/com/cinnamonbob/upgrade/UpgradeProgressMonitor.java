package com.cinnamonbob.upgrade;

import com.cinnamonbob.core.util.Constants;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * <class-comment/>
 */
public class UpgradeProgressMonitor
{
    private long startTimestamp;
    private long finishTimestamp;

    private int taskCount;
    private int tasksFinishedCount;

    private boolean error = false;

    private Map<String, UpgradeTaskProgress> taskProgress = new TreeMap<String, UpgradeTaskProgress>();

    protected void start()
    {
        startTimestamp = System.currentTimeMillis();
    }

    protected void stop()
    {
        finishTimestamp = System.currentTimeMillis();
    }

    protected void start(UpgradeTask task)
    {
        taskProgress.get(task.getName()).setStatus(UpgradeTaskProgress.IN_PROGRESS);
    }

    protected void complete(UpgradeTask task)
    {
        taskProgress.get(task.getName()).setStatus(UpgradeTaskProgress.COMPLETE);
        tasksFinishedCount++;
    }

    protected void failed(UpgradeTask task)
    {
        taskProgress.get(task.getName()).setStatus(UpgradeTaskProgress.FAILED);
        tasksFinishedCount++;
        error = true;
    }

    protected void aborted(UpgradeTask task)
    {
        taskProgress.get(task.getName()).setStatus(UpgradeTaskProgress.ABORTED);
        tasksFinishedCount++;
    }

    public boolean isComplete()
    {
        return tasksFinishedCount == taskCount && taskCount != 0;
    }

    public boolean isSuccessful()
    {
        return !error;
    }

    public boolean isError()
    {
        return error;
    }

    public String getElaspedTime()
    {
        if (startTimestamp == 0)
        {
            return "n/a";
        }

        long elapsedTime = 0;
        if (finishTimestamp != 0)
        {
            elapsedTime = finishTimestamp - startTimestamp;
        }
        else
        {
            elapsedTime = System.currentTimeMillis() - startTimestamp;
        }

        return "" + (elapsedTime / Constants.SECOND) + " second(s).";
    }

    public int getPercentageComplete()
    {
        return (int)(100*((float)tasksFinishedCount) / ((float)taskCount));
    }

    protected void setTasks(List<UpgradeTask> monitoredTasks)
    {
        for (UpgradeTask task : monitoredTasks)
        {
            taskProgress.put(task.getName(), new UpgradeTaskProgress(task));
        }
        taskCount = monitoredTasks.size();
    }

    public List<UpgradeTaskProgress> getTaskProgress()
    {
        return new LinkedList<UpgradeTaskProgress>(this.taskProgress.values());
    }
}
