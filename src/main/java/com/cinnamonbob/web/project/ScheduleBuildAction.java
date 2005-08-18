package com.cinnamonbob.web.project;

import com.cinnamonbob.BobServer;
import com.cinnamonbob.model.Project;

public class ScheduleBuildAction extends ProjectActionSupport
{
    private long id;
    private Project project;
    
    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

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
        project = getProjectManager().getProject(id);
        BobServer.build(project.getName());
        try
        {
            // Pause for dramatic effect
            Thread.sleep(1000);
        }
        catch(InterruptedException e)
        {
        }
        return SUCCESS;
    }
}
