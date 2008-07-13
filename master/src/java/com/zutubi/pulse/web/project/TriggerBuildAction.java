package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.ManualTriggerBuildReason;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.tove.config.project.ProjectConfiguration;

public class TriggerBuildAction extends ProjectActionBase
{
    public String execute()
    {
        Project project = getRequiredProject();
        getProjectManager().checkWrite(project);

        ProjectConfiguration projectConfig = project.getConfig();
        if(projectConfig.getOptions().getPrompt())
        {
            return "prompt";
        }
        
        getProjectManager().triggerBuild(projectConfig, new ManualTriggerBuildReason((String)getPrinciple()), null, true);

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
