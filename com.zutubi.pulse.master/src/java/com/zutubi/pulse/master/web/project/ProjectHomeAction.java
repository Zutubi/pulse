package com.zutubi.pulse.master.web.project;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.master.model.BuildColumns;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;

import java.util.List;

/**
 */
public class ProjectHomeAction extends ProjectActionBase
{
    private int totalBuilds;
    private int successfulBuilds;
    private int failedBuilds;
    private BuildResult currentBuild;
    private List<Changelist> latestChanges;
    private List<BuildResult> recentBuilds;
    private BuildColumns summaryColumns;
    private BuildColumns recentColumns;

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
        Project project = getRequiredProject();
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
        Project project = getRequiredProject();

        totalBuilds = buildManager.getBuildCount(project, ResultState.getCompletedStates());
        successfulBuilds = buildManager.getBuildCount(project, new ResultState[]{ResultState.SUCCESS});
        failedBuilds = buildManager.getBuildCount(project, new ResultState[]{ResultState.FAILURE});
        currentBuild = buildManager.getLatestBuildResult(project);
        latestChanges = buildManager.getLatestChangesForProject(project, 10);
        recentBuilds = buildManager.getLatestBuildResultsForProject(project, 11);
        if(!recentBuilds.isEmpty())
        {
            recentBuilds.remove(0);
        }

        User user = getLoggedInUser();
        summaryColumns = new BuildColumns(user == null ? UserPreferencesConfiguration.defaultProjectColumns() : user.getPreferences().getProjectSummaryColumns(), projectManager);
        recentColumns = new BuildColumns(user == null ? UserPreferencesConfiguration.defaultProjectColumns() : user.getPreferences().getProjectRecentColumns(), projectManager);

        return SUCCESS;
    }

}
