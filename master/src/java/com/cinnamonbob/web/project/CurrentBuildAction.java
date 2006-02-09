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


    public void validate()
    {
        project = getProjectManager().getProject(id);
        if (project == null)
        {
            addActionError("Unknown project [" + id + "]");
        }
    }

    public String execute()
    {
        if (project != null)
        {
            currentBuild = getBuildManager().getLatestBuildResult(project);
        }

        return SUCCESS;
    }
}
