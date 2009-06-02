package com.zutubi.pulse.master.cleanup;

import com.zutubi.pulse.master.scheduling.Task;
import com.zutubi.pulse.master.scheduling.TaskExecutionContext;

/**
 * A scheduled task that when executed will trigger a full project cleanup.
 */
public class CleanupBuilds implements Task
{
    private CleanupScheduler cleanupScheduler;

    public void execute(TaskExecutionContext context)
    {
        cleanupScheduler.scheduleProjectCleanup();
    }

    public void setCleanupScheduler(CleanupScheduler cleanupScheduler)
    {
        this.cleanupScheduler = cleanupScheduler;
    }
}