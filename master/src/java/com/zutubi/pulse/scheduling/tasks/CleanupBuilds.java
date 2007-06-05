package com.zutubi.pulse.scheduling.tasks;

import com.zutubi.pulse.model.CleanupManager;
import com.zutubi.pulse.scheduling.Task;
import com.zutubi.pulse.scheduling.TaskExecutionContext;
import com.zutubi.pulse.util.logging.Logger;

/**
 * <class-comment/>
 */
public class CleanupBuilds implements Task
{
    private static final Logger LOG = Logger.getLogger(CleanupBuilds.class);

    private CleanupManager cleanupManager;

    public void execute(TaskExecutionContext context)
    {
        LOG.info("cleaning up builds");
        cleanupManager.cleanupBuilds();
    }

    public void setCleanupManager(CleanupManager cleanupManager)
    {
        this.cleanupManager = cleanupManager;
    }
}