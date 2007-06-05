package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.ManualTriggerBuildReason;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;

public class TriggerBuildAction extends ProjectActionSupport
{
    private long id = -1;
    private String projectName;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void validate()
    {
    }

    public String execute()
    {
        Project project = getProjectManager().getProject(projectId);

        if (project == null)
        {
            addActionError("Trigger build request for unknown project [" + projectId + "]");
            return ERROR;
        }

        getProjectManager().checkWrite(project);

        ProjectConfiguration projectConfig = getProjectManager().getProjectConfig(projectId);

        if(projectConfig.getOptions().getPrompt())
        {
            projectName = projectConfig.getName();
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
