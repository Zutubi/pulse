package com.zutubi.pulse.upgrade;

import com.zutubi.pulse.util.TimeStamps;

import java.util.*;

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

    private int percentageComplete = 0;

    private boolean error = false;

    /**
     * Implementation note:
     *
     * Use a list here to ensure that the order of the task progress entries
     * remain in the same order as was passed to this monitor.
     */
    private List<UpgradeTaskProgress> orderedProgressDetails = new LinkedList<UpgradeTaskProgress>();

    private Map<UpgradeTask, UpgradeTaskProgress> progressLookupMap = new HashMap<UpgradeTask, UpgradeTaskProgress>();

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

    /**
     * Inform the progress monitor that the specified task has started.
     *
     * @param task
     */
    protected void start(UpgradeTask task)
    {
        getTaskProgress(task).setStatus(UpgradeTaskProgress.IN_PROGRESS);
    }

    /**
     * Inform the progress montior that the specified task has completed.
     *
     * @param task
     */
    protected void complete(UpgradeTask task)
    {
        getTaskProgress(task).setStatus(UpgradeTaskProgress.COMPLETE);
        tasksFinishedCount++;
    }

    /**
     * Inform the progress monitor that the specified task has failed.
     *
     * @param task
     */
    protected void failed(UpgradeTask task)
    {
        getTaskProgress(task).setStatus(UpgradeTaskProgress.FAILED);
        tasksFinishedCount++;
        error = true;
    }

    /**
     * Inform the progress monitor that the specified task has been aborted.
     *
     * @param task
     */
    protected void aborted(UpgradeTask task)
    {
        getTaskProgress(task).setStatus(UpgradeTaskProgress.ABORTED);
        tasksFinishedCount++;
    }

    /**
     * Returns true if all of the upgrade tasks being monitored have been completed, failed or aborted.
     *
     */
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

    public int getPercentageComplete()
    {
        if (percentageComplete != 0)
        {
            return percentageComplete;
        }
        return (int) (100 * ((float) tasksFinishedCount) / ((float) taskCount));
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
            UpgradeTaskProgress progress = new UpgradeTaskProgress(task);
            progressLookupMap.put(task, progress);
            orderedProgressDetails.add(progress);
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
        return Collections.unmodifiableList(this.orderedProgressDetails);
    }

    /**
     * Retrieve information about the status of the individual upgrade tasks.
     *
     */
    public UpgradeTaskProgress getTaskProgress(UpgradeTask task)
    {
        return progressLookupMap.get(task);
    }
}
