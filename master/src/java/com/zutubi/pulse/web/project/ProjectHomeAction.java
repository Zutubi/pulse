package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.*;

import java.util.List;

/**
 */
public class ProjectHomeAction extends ProjectActionSupport
{
    private long id;
    private String projectName;
    private Project project;
    private int totalBuilds;
    private int successfulBuilds;
    private int failedBuilds;
    private BuildResult currentBuild;
    private List<Changelist> latestChanges;
    private List<BuildResult> recentBuilds;
    private BuildColumns summaryColumns;
    private BuildColumns recentColumns;

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

    public BuildColumns getSummaryColumns()
    {
        return summaryColumns;
    }

    public BuildColumns getRecentColumns()
    {
        return recentColumns;
    }

    public String execute()
    {
        if(id != 0)
        {
            project = getProjectManager().getProject(id);
        }
        else
        {
            project = getProjectManager().getProject(projectName);
        }

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

            User user = getLoggedInUser();
            summaryColumns = new BuildColumns(user == null ? User.getDefaultProjectColumns() : user.getProjectSummaryColumns(), projectManager);
            recentColumns = new BuildColumns(user == null ? User.getDefaultProjectColumns() : user.getProjectRecentColumns(), projectManager);
        }
        else
        {
            addActionError("Unknown project [" + id + "] " + projectName);
            return ERROR;
        }

        return SUCCESS;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }
}
