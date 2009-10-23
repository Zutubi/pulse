package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.actions.ActionManager;

/**
 * Manually trigger a project build.
 *
 * If the projects build options indicate that we should prompt for more input,
 * then we return "prompt" without triggering a build.
 */
public class TriggerBuildAction extends ProjectActionBase
{
    private ActionManager actionManager;

    private boolean rebuild = false;

    public boolean isRebuild()
    {
        return rebuild;
    }

    public void setRebuild(boolean rebuild)
    {
        this.rebuild = rebuild;
    }

    public String execute()
    {
        Project project = getRequiredProject();
        ProjectConfiguration projectConfig = project.getConfig();

        actionManager.ensurePermission(projectConfig.getConfigurationPath(), ProjectConfigurationActions.ACTION_TRIGGER);

        if(projectConfig.getOptions().getPrompt())
        {
            return "prompt";
        }

        if (rebuild)
        {
            actionManager.execute(ProjectConfigurationActions.ACTION_REBUILD, projectConfig, null);
        }
        else
        {
            actionManager.execute(ProjectConfigurationActions.ACTION_TRIGGER, projectConfig, null);
        }

        pauseForDramaticEffect();

        return SUCCESS;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }
}
