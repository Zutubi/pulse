package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Project;

/**
 * 
 *
 */
public class ManageProjectAction extends BaseProjectAction
{

    private long id;

    private Project project;

    public long getId()
    {
        return id;
    }

    public Project getProject()
    {
        return project;
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
        return SUCCESS;
    }
}
