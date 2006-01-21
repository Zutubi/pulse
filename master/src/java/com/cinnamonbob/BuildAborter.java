package com.cinnamonbob;

import com.cinnamonbob.model.BuildManager;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.ProjectManager;

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
