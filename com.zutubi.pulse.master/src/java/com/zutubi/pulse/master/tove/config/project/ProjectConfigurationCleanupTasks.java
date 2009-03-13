package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.tove.config.cleanup.RecordCleanupTask;

import java.util.Arrays;
import java.util.List;

/**
 * Adds a custom cleanup task for project configuration that deletes the
 * project state and build results.
 */
public class ProjectConfigurationCleanupTasks
{
    private ProjectManager projectManager;
    private TransactionContext transactionContext;

    public List<RecordCleanupTask> getTasks(ProjectConfiguration instance)
    {
        return Arrays.<RecordCleanupTask>asList(new ProjectStateCleanupTask(instance, projectManager, transactionContext));
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setTransactionContext(TransactionContext transactionContext)
    {
        this.transactionContext = transactionContext;
    }
}
