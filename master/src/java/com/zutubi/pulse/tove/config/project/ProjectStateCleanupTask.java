package com.zutubi.pulse.tove.config.project;

import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.tove.config.DatabaseStateCleanupTaskSupport;
import com.zutubi.tove.config.ToveRuntimeException;

/**
 * Cleans up the state associated with a deleted project.
 */
class ProjectStateCleanupTask extends DatabaseStateCleanupTaskSupport
{
    private ProjectConfiguration instance;
    private ProjectManager projectManager;

    public ProjectStateCleanupTask(ProjectConfiguration instance, ProjectManager projectManager, BuildManager buildManager)
    {
        super(instance.getConfigurationPath(), buildManager);
        this.projectManager = projectManager;
        this.instance = instance;
    }

    public void cleanupState()
    {
        Project project = projectManager.getProject(instance.getProjectId(), true);
        if (project != null)
        {
            // We need to make sure that the project is not building first,
            // so pause it and wait for the state to become paused.
            project = projectManager.pauseProject(project);
            if(project.getState() != Project.State.PAUSED)
            {
                throw new ToveRuntimeException("Unable to delete project as a build is running.  The project may be deleted when the build completes (consider pausing the project).");
            }

            projectManager.delete(project);
        }
    }
}
