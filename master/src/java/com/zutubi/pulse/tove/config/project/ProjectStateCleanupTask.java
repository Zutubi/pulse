package com.zutubi.pulse.tove.config.project;

import com.zutubi.pulse.core.PulseRuntimeException;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.tove.config.cleanup.RecordCleanupTaskSupport;

/**
 * Cleans up the state associated with a deleted project.
 */
class ProjectStateCleanupTask extends RecordCleanupTaskSupport
{
    private ProjectConfiguration instance;
    private ProjectManager projectManager;

    public ProjectStateCleanupTask(ProjectConfiguration instance, ProjectManager projectManager)
    {
        super(instance.getConfigurationPath());
        this.projectManager = projectManager;
        this.instance = instance;
    }

    public void run()
    {
        Project project = projectManager.getProject(instance.getProjectId(), true);
        if (project != null)
        {
            // We need to make sure that the project is not building first,
            // so pause it and wait for the state to become paused.
            project = projectManager.pauseProject(project);
            while(project != null && project.getState() != Project.State.PAUSED)
            {
                try
                {
                    Thread.sleep(60000);
                }
                catch (InterruptedException e)
                {
                    throw new PulseRuntimeException(e);
                }

                project = projectManager.getProject(instance.getProjectId(), true);
            }

            if(project != null)
            {
                projectManager.delete(project);
            }
        }
    }

    public boolean isAsynchronous()
    {
        return true;
    }
}
