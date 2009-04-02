package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.ManualTriggerBuildReason;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.master.tove.config.project.ResourcePropertyConfiguration;

import java.util.Collections;

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
        
        getProjectManager().triggerBuild(projectConfig, Collections.<ResourcePropertyConfiguration>emptyList(), new ManualTriggerBuildReason((String)getPrinciple()), null, ProjectManager.TRIGGER_CATEGORY_MANUAL, false, true);

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
