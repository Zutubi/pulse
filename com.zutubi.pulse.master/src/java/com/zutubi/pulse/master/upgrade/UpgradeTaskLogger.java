package com.zutubi.pulse.master.upgrade;

import com.zutubi.pulse.master.util.monitor.JobListener;
import com.zutubi.pulse.master.util.monitor.TaskFeedback;
import com.zutubi.util.logging.Logger;

/**
 * The logger is responsible for logging the execution of
 * upgrade tasks to the logging system.
 */
public class UpgradeTaskLogger implements JobListener<UpgradeTask>
{
    private static final Logger LOG = Logger.getLogger(UpgradeTaskLogger.class);

    public void taskStarted(UpgradeTask task, TaskFeedback<UpgradeTask> feedback)
    {
        // noop.
    }

    public void taskFailed(UpgradeTask task, TaskFeedback<UpgradeTask> feedback)
    {
        LOG.info("Task " + task.getName() + " failed in " + feedback.getElapsedTimePretty() + ". " + feedback.getStatusMessage());
    }

    public void taskCompleted(UpgradeTask task, TaskFeedback<UpgradeTask> feedback)
    {
        LOG.info("Task " + task.getName() + " completed in " + feedback.getElapsedTimePretty() + ".");
    }

    public void taskAborted(UpgradeTask task, TaskFeedback feedback)
    {
        LOG.info("Task " + task.getName() + " aborted.");
    }
}
