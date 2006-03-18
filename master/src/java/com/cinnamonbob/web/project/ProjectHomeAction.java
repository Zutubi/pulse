package com.cinnamonbob.web.project;

import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;
import com.opensymphony.util.TextUtils;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class ProjectHomeAction extends ProjectActionSupport
{
    private long id;
    private Project project;
    private BuildResult currentBuild;
    private List<Changelist> latestChanges;
    private LinkedList<BuildResult> changeBuilds;

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

    public boolean getHasBasics()
    {
        return TextUtils.stringSet(project.getDescription()) || TextUtils.stringSet(project.getUrl());
    }
    
    public BuildResult getCurrentBuild()
    {
        return currentBuild;
    }

    public List<Changelist> getLatestChanges()
    {
        return latestChanges;
    }

    public LinkedList<BuildResult> getChangeBuilds()
    {
        return changeBuilds;
    }

    public String execute()
    {
        project = getProjectManager().getProject(id);
        if (project != null)
        {
            currentBuild = getBuildManager().getLatestBuildResult(project);
            latestChanges = getBuildManager().getLatestChangesForProject(project, 10);
            changeBuilds = new LinkedList<BuildResult>();

            for (Changelist list : latestChanges)
            {
                BuildResult build = getBuildManager().getBuildResult(list.getResultId());
                changeBuilds.add(build);
            }
        }
        else
        {
            addActionError("Unknown project [" + id + "]");
            return ERROR;
        }

        return SUCCESS;
    }
}
