package com.zutubi.pulse.upgrade;

import com.zutubi.pulse.util.TimeStamps;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The Upgrade Progress Monitor is a class used to help track and display the status of the upgrade.
 *
 *
 */
public class UpgradeProgressMonitor
{
    /**
     * The upgrade process start time.
     */
    private long startTimestamp;

    /**
     * The upgrade process finish time
     */
    private long finishTimestamp;

    private int taskCount;
    private int tasksFinishedCount;

    private int percentageComplete;

    /**
     * Implementation note:
     * <p/>
     * Use a list here to ensure that the order of the task progress entries
     * remain in the same order as was passed to this monitor.
     */
    private List<TaskGroupUpgradeProgress> orderedTaskGroups = new LinkedList<TaskGroupUpgradeProgress>();

    private Map<UpgradeTask, TaskUpgradeProgress> taskProgressLookupMap = new HashMap<UpgradeTask, TaskUpgradeProgress>();
    private Map<UpgradeTaskGroup, TaskGroupUpgradeProgress> groupProgressLookupMap = new HashMap<UpgradeTaskGroup, TaskGroupUpgradeProgress>();

    public void start()
    {
        startTimestamp = System.currentTimeMillis();
    }

    public void finish()
    {
        finishTimestamp = System.currentTimeMillis();
    }

    /**
     * Return the elapsed time.
     *
     * @return formatted string representing the elapsed time.
     */
    public String getElaspedTime()
    {
        // if start timestamp is zero, we have not started.
        if (startTimestamp == 0)
        {
            return TimeStamps.getPrettyElapsed(0);
        }

        long elapsedTime;
        // if finish time is zero, then we have not finished.
        if (finishTimestamp == 0)
        {
            elapsedTime = System.currentTimeMillis() - startTimestamp;
        }
        else
        {
            elapsedTime = finishTimestamp - startTimestamp;
        }
        return TimeStamps.getPrettyElapsed(elapsedTime);
    }

    public TaskUpgradeProgress getProgress(UpgradeTask task)
    {
        return taskProgressLookupMap.get(task);
    }

    public TaskGroupUpgradeProgress getProgress(UpgradeTaskGroup group)
    {
        return groupProgressLookupMap.get(group);
    }

    /**
     * Notify the monitor that the specified upgrade task has started / is starting processing.
     *
     * @param task in question
     */
    public void started(UpgradeTask task)
    {
        TaskUpgradeProgress taskProgress = getProgress(task);
        taskProgress.setStatus(UpgradeStatus.IN_PROGRESS);
    }

    /**
     * Notify the monitor that the specified upgrade task has been aborted.
     *
     * @param task
     */
    public void aborted(UpgradeTask task)
    {
        TaskUpgradeProgress taskProgress = getProgress(task);
        taskProgress.setStatus(UpgradeStatus.ABORTED);

        tasksFinishedCount++;
    }

    /**
     * Notify the monitor that the specified upgrade task has failed.
     *
     * @param task
     */
    public void failed(UpgradeTask task)
    {
        TaskUpgradeProgress taskProgress = getProgress(task);
        taskProgress.setStatus(UpgradeStatus.FAILED);

        tasksFinishedCount++;
        error = true;
    }

    /**
     * Notify the monitor that the specified upgrade task has completed successfully.
     *
     * @param task
     */
    public void completed(UpgradeTask task)
    {
        TaskUpgradeProgress taskProgress = getProgress(task);
        taskProgress.setStatus(UpgradeStatus.COMPLETED);

        tasksFinishedCount++;
    }

    public int getPercentageComplete()
    {
        if (percentageComplete != 0)
        {
            return percentageComplete;
        }

        if (taskCount == 0)
        {
            return 100;
        }
        else
        {
            return (int) (100 * ((float) tasksFinishedCount) / ((float) taskCount));
        }
    }

    /**
     * Manually specify the percentage complete value. If not set, then the percentage complete will
     * be reported as the percentage of tasks finished.
     *
     * @param percentageComplete
     */
    public void setPercentageComplete(int percentageComplete)
    {
        this.percentageComplete = percentageComplete;
    }

    public void setTaskGroups(List<UpgradeTaskGroup> monitoredGroups)
    {
        taskCount = 0;

        for (UpgradeTaskGroup group : monitoredGroups)
        {
            TaskGroupUpgradeProgress groupProgress = new TaskGroupUpgradeProgress(group);
            groupProgressLookupMap.put(group, groupProgress);
            orderedTaskGroups.add(groupProgress);

            List<UpgradeTask> monitoredTasks = group.getTasks();
            for (UpgradeTask task : monitoredTasks)
            {
                TaskUpgradeProgress taskProgress = new TaskUpgradeProgress(task);
                taskProgressLookupMap.put(task, taskProgress);
                groupProgress.add(taskProgress);
            }
            taskCount = taskCount + monitoredTasks.size();
        }
    }

    public List<TaskGroupUpgradeProgress> getOrderedTaskGroups()
    {
        return orderedTaskGroups;
    }

    public void started(UpgradeTaskGroup group)
    {
        TaskGroupUpgradeProgress groupProgress = getProgress(group);
        groupProgress.setStatus(UpgradeStatus.IN_PROGRESS);
    }

    public void completed(UpgradeTaskGroup group)
    {
        TaskGroupUpgradeProgress groupProgress = getProgress(group);
        groupProgress.setStatus(UpgradeStatus.COMPLETED);
    }

    public void aborted(UpgradeTaskGroup group)
    {
        TaskGroupUpgradeProgress groupProgress = getProgress(group);
        groupProgress.setStatus(UpgradeStatus.ABORTED);
    }

    public boolean isStarted()
    {
        return startTimestamp != 0;
    }

    private boolean error = false;

    public boolean isSuccessful()
    {
        return !error;
    }

    public boolean isError()
    {
        return error;
    }
}
