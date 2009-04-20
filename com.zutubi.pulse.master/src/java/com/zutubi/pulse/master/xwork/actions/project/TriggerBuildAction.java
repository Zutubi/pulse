package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.ManualTriggerBuildReason;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.TriggerOptions;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;

/**
 * Manually trigger a project build.
 *
 * If the projects build options indicate that we should prompt for more input,
 * then we return "prompt" without triggering a build.
 */
public class TriggerBuildAction extends ProjectActionBase
{
    public String execute()
    {
        Project project = getRequiredProject();
        accessManager.ensurePermission(ProjectConfigurationActions.ACTION_TRIGGER, project);

        ProjectConfiguration projectConfig = project.getConfig();
        if(projectConfig.getOptions().getPrompt())
        {
            return "prompt";
        }
        
        TriggerOptions options = new TriggerOptions(new ManualTriggerBuildReason(getPrinciple()), ProjectManager.TRIGGER_CATEGORY_MANUAL);
        getProjectManager().triggerBuild(projectConfig, options, null);

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
}
