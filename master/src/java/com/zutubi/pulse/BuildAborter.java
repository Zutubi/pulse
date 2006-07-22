package com.zutubi.pulse;

import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;

import java.util.List;

/**
 */
public class BuildAborter implements Runnable
{
    private ProjectManager projectManager;
    private BuildManager buildManager;

    public void run()
    {
        List<Project> projects = projectManager.getAllProjects();
        for (Project project : projects)
        {
            buildManager.abortUnfinishedBuilds(project, "Server shut down while build in progress");
            if(project.getState() == Project.State.BUILDING || project.getState() == Project.State.PAUSING)
            {
                projectManager.buildCompleted(project.getId());
            }
        }
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

}
