package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.ManualTriggerBuildReason;
import com.zutubi.pulse.model.Project;

public class TriggerBuildAction extends ProjectActionSupport
{
    private long id = -1;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
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

        BuildSpecification spec;
        if(id > 0)
        {
            spec = project.getBuildSpecification(id);
        }
        else
        {
            spec = project.getDefaultSpecification();
        }

        if (spec == null)
        {
            addActionError("Request to build unknown build specification id '" + id + "' for project '" + project.getName() + "'");
            return ERROR;
        }

        if(spec.getPrompt())
        {
            return "prompt";
        }
        
        getProjectManager().triggerBuild(project, spec.getName(), new ManualTriggerBuildReason((String)getPrinciple()), null, true);

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
