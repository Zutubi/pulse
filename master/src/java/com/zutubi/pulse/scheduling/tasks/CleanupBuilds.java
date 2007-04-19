package com.zutubi.pulse.scheduling.tasks;

import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.scheduling.Task;
import com.zutubi.pulse.scheduling.TaskExecutionContext;
import com.zutubi.util.logging.Logger;

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