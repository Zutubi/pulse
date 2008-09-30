package com.zutubi.pulse.cleanup;

import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.scheduling.Task;
import com.zutubi.pulse.scheduling.TaskExecutionContext;
import com.zutubi.util.logging.Logger;

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