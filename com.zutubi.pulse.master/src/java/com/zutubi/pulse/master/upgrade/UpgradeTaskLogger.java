package com.zutubi.pulse.master.upgrade;

import com.zutubi.pulse.master.util.monitor.JobListener;
import com.zutubi.pulse.master.util.monitor.Task;
import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.pulse.master.util.monitor.TaskFeedback;
import com.zutubi.util.logging.Logger;

/**
 * The logger is responsible for logging the execution of
 * upgrade tasks to the logging system.
 */
public class UpgradeTaskLogger implements JobListener
{
    private static final Logger LOG = Logger.getLogger(UpgradeTaskLogger.class);

    private Monitor monitor;

    public UpgradeTaskLogger(Monitor monitor)
    {
        this.monitor = monitor;
    }

    public void taskStarted(Task task)
    {
        // noop.
    }

    public void taskFailed(Task task)
    {
        TaskFeedback feedback = monitor.getProgress(task);
        LOG.info("Task " + task.getName() + " failed in " + feedback.getElapsedTimePretty() + ". " + feedback.getStatusMessage());
    }

    public void taskCompleted(Task task)
    {
        TaskFeedback feedback = monitor.getProgress(task);
        LOG.info("Task " + task.getName() + " completed in " + feedback.getElapsedTimePretty() + ".");
    }

    public void taskAborted(Task task)
    {
        LOG.info("Task " + task.getName() + " aborted.");
    }

}
