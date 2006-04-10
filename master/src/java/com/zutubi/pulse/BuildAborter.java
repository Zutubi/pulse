package com.zutubi.pulse;

import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
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
            BuildResult lastBuild = buildManager.getLatestBuildResult(project);
            if (lastBuild != null && !lastBuild.completed())
            {
                lastBuild.abortUnfinishedRecipes();
                lastBuild.error("Server shut down while build in progress");
                lastBuild.complete();
                buildManager.save(lastBuild);
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
