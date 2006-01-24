package com.cinnamonbob.scheduling.tasks;

import com.cinnamonbob.model.BuildManager;
import com.cinnamonbob.scheduling.Task;
import com.cinnamonbob.scheduling.TaskExecutionContext;
import com.cinnamonbob.util.logging.Logger;

/**
 * <class-comment/>
 */
public class CleanupBuilds implements Task
{
    private static final Logger LOG = Logger.getLogger(CleanupBuilds.class);

    private BuildManager buildManager;

    public void execute(TaskExecutionContext context)
    {
        LOG.info("cleaning up builds");
        buildManager.cleanupBuilds();
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}