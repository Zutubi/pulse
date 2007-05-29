package com.zutubi.pulse.prototype.config;

import com.zutubi.pulse.model.ManualTriggerBuildReason;
import com.zutubi.pulse.model.ProjectManager;

/**
 *
 *
 */
public class ProjectConfigurationActionHandler
{
    private ProjectManager projectManager;

    public void doTrigger(ProjectConfiguration projectConfig)
    {
        projectManager.triggerBuild(projectConfig, new ManualTriggerBuildReason("manual"), null, true);
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
