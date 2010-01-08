package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.build.queue.BuildQueueSnapshot;
import com.zutubi.pulse.master.build.queue.SchedulingController;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.pulse.master.tove.model.ActionLink;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.security.AccessManager;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions.*;
import static java.util.Arrays.asList;

/**
 * Action to display project home page - the latest project status.
 */
public class ProjectHomeAction extends ProjectActionBase
{
    private int totalBuilds;
    private int successfulBuilds;
    private int failedBuilds;
    private int queuedBuilds;
    private boolean paused;
    private boolean pausable;
    private boolean resumable;
    private String responsibleOwner;
    private String responsibleComment;
    private boolean canClearResponsible = false;
    private BuildResult currentBuild;
    private List<PersistentChangelist> latestChanges;
    private List<BuildResult> recentBuilds;
    private BuildColumns summaryColumns;
    private BuildColumns recentColumns;
    private List<ActionLink> actions = new LinkedList<ActionLink>();

    private ActionManager actionManager;
    private SystemPaths systemPaths;
    private SchedulingController schedulingController;

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

    public int getQueuedBuilds()
    {
        return queuedBuilds;
    }

    public boolean isPaused()
    {
        return paused;
    }

    public boolean isPausable()
    {
        return pausable;
    }

    public boolean isResumable()
    {
        return resumable;
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

    public String getResponsibleOwner()
    {
        return responsibleOwner;
    }

    public String getResponsibleComment()
    {
        return responsibleComment;
    }

    public boolean isCanClearResponsible()
    {
        return canClearResponsible;
    }

    public BuildResult getCurrentBuild()
    {
        return currentBuild;
    }

    public List<PersistentChangelist> getLatestChanges()
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

    public List<ActionLink> getActions()
    {
        return actions;
    }

    public String execute()
    {
        Project project = getRequiredProject();
        
        paused = project.getState() == Project.State.PAUSED;
        pausable = project.isTransitionValid(Project.Transition.PAUSE);
        resumable = project.isTransitionValid(Project.Transition.RESUME);

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
        summaryColumns = new BuildColumns(user == null ? UserPreferencesConfiguration.defaultProjectColumns() : user.getPreferences().getProjectSummaryColumns(), accessManager);
        recentColumns = new BuildColumns(user == null ? UserPreferencesConfiguration.defaultProjectColumns() : user.getPreferences().getProjectRecentColumns(), accessManager);

        File contentRoot = systemPaths.getContentRoot();
        List<String> availableActions = actionManager.getActions(project.getConfig(), false, true);
        if (accessManager.hasPermission(AccessManager.ACTION_WRITE, project.getConfig()))
        {
            availableActions.add(AccessManager.ACTION_WRITE);
        }
        
        Messages messages = Messages.getInstance(ProjectConfiguration.class);
        for (String candidateAction: asList(AccessManager.ACTION_WRITE, ACTION_MARK_CLEAN, ACTION_TRIGGER, ACTION_REBUILD))
        {
            if (availableActions.contains(candidateAction))
            {
                actions.add(ToveUtils.getActionLink(candidateAction, messages, contentRoot));
            }
        }

        addResponsibilityActions(project, messages, contentRoot);

        BuildQueueSnapshot snapshot = schedulingController.getSnapshot();
        queuedBuilds = snapshot.getQueuedRequestsByOwner(project).size();

        return SUCCESS;
    }

    private void addResponsibilityActions(Project project, Messages messages, File contentRoot)
    {
        ProjectResponsibility projectResponsibility = project.getResponsibility();
        if (projectResponsibility == null && accessManager.hasPermission(ACTION_TAKE_RESPONSIBILITY, project))
        {
            actions.add(ToveUtils.getActionLink(ACTION_TAKE_RESPONSIBILITY, messages, contentRoot));
        }

        if (projectResponsibility != null)
        {
            responsibleOwner = projectResponsibility.getMessage(getLoggedInUser());
            responsibleComment = projectResponsibility.getComment();

            if (accessManager.hasPermission(ACTION_CLEAR_RESPONSIBILITY, project))
            {
                canClearResponsible = true;
                actions.add(ToveUtils.getActionLink(ACTION_CLEAR_RESPONSIBILITY, messages, contentRoot));
            }
        }
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }

    public void setSchedulingController(SchedulingController schedulingController)
    {
        this.schedulingController = schedulingController;
    }
}
