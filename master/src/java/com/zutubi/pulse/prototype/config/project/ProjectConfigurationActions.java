package com.zutubi.pulse.prototype.config.project;

import com.zutubi.config.annotations.Permission;
import com.zutubi.pulse.model.ManualTriggerBuildReason;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.security.AcegiUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Action links for the project config page.
 */
public class ProjectConfigurationActions
{
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_RESUME = "resume";
    public static final String ACTION_TRIGGER = "trigger";

    private ProjectManager projectManager;

    public List<String> getActions(ProjectConfiguration instance)
    {
        List<String> result = new LinkedList<String>();
        result.add(ACTION_TRIGGER);
        Project project = projectManager.getProject(instance.getProjectId(), true);
        if (project != null)
        {
            if (project.isPaused())
            {
                result.add(ACTION_RESUME);
            }
            else
            {
                result.add(ACTION_PAUSE);
            }
        }

        return result;
    }

    @Permission(ACTION_TRIGGER)
    public void doTrigger(ProjectConfiguration projectConfig)
    {
        String user = AcegiUtils.getLoggedInUsername();
        if (user != null)
        {
            projectManager.triggerBuild(projectConfig, new ManualTriggerBuildReason(user), null, true);
        }
    }

    @Permission(ACTION_PAUSE)
    public void doPause(ProjectConfiguration projectConfig)
    {
        Project project = projectManager.getProject(projectConfig.getProjectId(), true);
        if (project != null)
        {
            projectManager.pauseProject(project);
        }
    }

    @Permission(ACTION_PAUSE)
    public void doResume(ProjectConfiguration projectConfig)
    {
        Project project = projectManager.getProject(projectConfig.getProjectId(), true);
        if (project != null)
        {
            projectManager.resumeProject(project);
        }
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
