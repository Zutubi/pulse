package com.zutubi.pulse.master.cleanup;

import com.zutubi.pulse.master.scheduling.Task;
import com.zutubi.pulse.master.scheduling.TaskExecutionContext;

/**
 * <class-comment/>
 */
public class CleanupBuilds implements Task
{
    private CleanupManager cleanupManager;

    public void execute(TaskExecutionContext context)
    {
        cleanupManager.cleanupBuilds();
    }

    public void setCleanupManager(CleanupManager cleanupManager)
    {
        this.cleanupManager = cleanupManager;
    }
}