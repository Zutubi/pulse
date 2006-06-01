package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ChangelistUtils;

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
    private List<BuildResult> recentBuilds;

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

    public boolean getProjectNotBuilding()
    {
        return project.getState() == Project.State.PAUSED || project.getState() == Project.State.IDLE;
    }

    public BuildResult getCurrentBuild()
    {
        return currentBuild;
    }

    public List<Changelist> getLatestChanges()
    {
        return latestChanges;
    }

    public List<BuildResult> getRecentBuilds()
    {
        return recentBuilds;
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
            recentBuilds = buildManager.getLatestBuildResultsForProject(project, 11);
            if(!recentBuilds.isEmpty())
            {
                recentBuilds.remove(0);
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
