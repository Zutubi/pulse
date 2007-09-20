package com.zutubi.pulse.prototype.config.project;

import com.zutubi.pulse.model.ManualTriggerBuildReason;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.security.AcegiUtils;

/**
 * Action links for the project config page.
 */
public class ProjectConfigurationActions
{
    public static final String ACTION_PAUSE   = "pause";
    public static final String ACTION_TRIGGER = "trigger";

    private ProjectManager projectManager;

    public void doTrigger(ProjectConfiguration projectConfig)
    {
        String user = AcegiUtils.getLoggedInUsername();
        if (user != null)
        {
            projectManager.triggerBuild(projectConfig, new ManualTriggerBuildReason(user), null, true);
        }
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
