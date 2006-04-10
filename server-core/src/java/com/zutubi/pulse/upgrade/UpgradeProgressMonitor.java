package com.cinnamonbob.upgrade;

import com.cinnamonbob.core.util.TimeStamps;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class UpgradeProgressMonitor
{
    private long startTimestamp;
    private long finishTimestamp;

    private int taskCount;
    private int tasksFinishedCount;

    private int percentageComplete = 0;

    private boolean error = false;

    /**
     * Implementation note:
     * Use a list here to ensure that the order of the task progress entries
     * remain in the same order as was passed to this monitor.
     */
    private List<UpgradeTaskProgress> taskProgress = new LinkedList<UpgradeTaskProgress>();

    /**
     * Start the progress monitor.
     */
    protected void start()
    {
        startTimestamp = System.currentTimeMillis();
        percentageComplete = 0;
    }

    /**
     * Stop the progress monitor.
     */
    protected void stop()
    {
        finishTimestamp = System.currentTimeMillis();
    }

    protected void start(UpgradeTask task)
    {
        getTaskProgress(task.getName()).setStatus(UpgradeTaskProgress.IN_PROGRESS);
    }

    protected void complete(UpgradeTask task)
    {
        getTaskProgress(task.getName()).setStatus(UpgradeTaskProgress.COMPLETE);
        tasksFinishedCount++;
    }

    protected void failed(UpgradeTask task)
    {
        getTaskProgress(task.getName()).setStatus(UpgradeTaskProgress.FAILED);
        tasksFinishedCount++;
        error = true;
    }

    protected void aborted(UpgradeTask task)
    {
        getTaskProgress(task.getName()).setStatus(UpgradeTaskProgress.ABORTED);
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

    /**
     * Return the elapsed time.
     *
     * @return formatted string representing the elapsed time.
     */
    public String getElaspedTime()
    {
        if (startTimestamp == 0)
        {
            return TimeStamps.getPrettyElapsed(0);
        }

        long elapsedTime;
        if (finishTimestamp != 0)
        {
            elapsedTime = finishTimestamp - startTimestamp;
        }
        else
        {
            elapsedTime = System.currentTimeMillis() - startTimestamp;
        }
        return TimeStamps.getPrettyElapsed(elapsedTime);
    }

    public int getPercentageComplete()
    {
        if (percentageComplete != 0)
        {
            return percentageComplete;
        }
        return (int) (100 * ((float) tasksFinishedCount) / ((float) taskCount));
    }

    public void setPercentageComplete(int percentageComplete)
    {
        this.percentageComplete = percentageComplete;
    }

    /**
     * Specify the list of upgrade tasks that are being monitored. Note: the order
     * of this list should mirror the order in which the tasks are executed.
     *
     * @param monitoredTasks
     */
    protected void setTasks(List<UpgradeTask> monitoredTasks)
    {
        for (UpgradeTask task : monitoredTasks)
        {
            taskProgress.add(new UpgradeTaskProgress(task));
        }
        taskCount = monitoredTasks.size();
    }

    /**
     * Retrieve information about the status of the upgrade tasks.
     *
     * @return the list of task progress entries.
     */
    public List<UpgradeTaskProgress> getTaskProgress()
    {
        return Collections.unmodifiableList(this.taskProgress);
    }

    /**
     * Retrieve information about the status of the individual upgrade tasks.
     *
     */
    public UpgradeTaskProgress getTaskProgress(String taskName)
    {
        for (UpgradeTaskProgress progress : taskProgress)
        {
            if (progress.getName().equals(taskName))
            {
                return progress;
            }
        }
        return null;
    }
}
