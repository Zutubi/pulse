package com.cinnamonbob.web.project;

import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;

/**
 */
public class CurrentBuildAction extends ProjectActionSupport
{
    private long id;
    private Project project;
    private BuildResult currentBuild;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Project getProject()
    {
        return project;
    }

    public BuildResult getCurrentBuild()
    {
        return currentBuild;
    }

    public String execute()
    {
        project = getProjectManager().getProject(id);
        if (project != null)
        {
            currentBuild = getBuildManager().getLatestBuildResult(project);
        }
        else
        {
            addActionError("Unknown project [" + id + "]");
            return ERROR;
        }

        return SUCCESS;
    }
}
