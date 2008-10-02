package com.zutubi.pulse.web.project;

import com.zutubi.pulse.master.model.ManualTriggerBuildReason;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

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
        
        getProjectManager().triggerBuild(projectConfig, new ManualTriggerBuildReason((String)getPrinciple()), null, ProjectManager.TRIGGER_CATEGORY_MANUAL, false, true);

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
