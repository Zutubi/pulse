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

    public String execute()
    {
        Project project = getRequiredProject();
        ProjectConfiguration projectConfig = project.getConfig();

        actionManager.ensurePermission(projectConfig.getConfigurationPath(), ProjectConfigurationActions.ACTION_TRIGGER);

        if(projectConfig.getOptions().getPrompt())
        {
            return "prompt";
        }
        
        actionManager.execute(ProjectConfigurationActions.ACTION_TRIGGER, projectConfig, null);

        try
        {
            // Pause for dramatic effect
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            // Empty
        }

        return SUCCESS;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }
}
