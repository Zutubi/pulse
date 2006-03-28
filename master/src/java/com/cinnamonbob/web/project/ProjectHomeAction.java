package com.cinnamonbob.web.project;

import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.core.model.ResultState;
import com.cinnamonbob.model.BuildManager;
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
    private int totalBuilds;
    private int successfulBuilds;
    private int failedBuilds;
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

    public int getTotalBuilds()
    {
        return totalBuilds;
    }

    public int getSuccessfulBuilds()
    {
        return successfulBuilds;
    }

    public int getFailedBuilds()
    {
        return failedBuilds;
    }

    public int getErrorBuilds()
    {
        return totalBuilds - successfulBuilds - failedBuilds;
    }

    public int getPercent(int quotient, int divisor)
    {
        if (divisor > 0)
        {
            return (int) Math.round(quotient * 100.0 / divisor);
        }
        else
        {
            return 0;
        }
    }

    public int getPercentSuccessful()
    {
        return getPercent(successfulBuilds, totalBuilds);
    }

    public int getPercentSuccessNoErrors()
    {
        return getPercent(successfulBuilds, totalBuilds - getErrorBuilds());
    }

    public int getPercentFailed()
    {
        return getPercent(failedBuilds, totalBuilds);
    }

    public int getPercentError()
    {
        return getPercent(getErrorBuilds(), totalBuilds);
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
            BuildManager buildManager = getBuildManager();
            totalBuilds = buildManager.getBuildCount(project, new ResultState[]{ResultState.SUCCESS, ResultState.ERROR, ResultState.FAILURE}, null);
            successfulBuilds = buildManager.getBuildCount(project, new ResultState[]{ResultState.SUCCESS}, null);
            failedBuilds = buildManager.getBuildCount(project, new ResultState[]{ResultState.FAILURE}, null);
            currentBuild = buildManager.getLatestBuildResult(project);
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
