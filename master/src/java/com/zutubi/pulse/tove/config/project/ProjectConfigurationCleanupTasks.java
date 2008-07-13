package com.zutubi.pulse.tove.config.project;

import com.zutubi.pulse.model.ProjectManager;
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

    public List<RecordCleanupTask> getTasks(ProjectConfiguration instance)
    {
        return Arrays.<RecordCleanupTask>asList(new ProjectStateCleanupTask(instance, projectManager));
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
