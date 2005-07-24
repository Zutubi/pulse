package com.cinnamonbob.web;

import com.opensymphony.xwork.ActionSupport;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.ProjectManager;

/**
 * 
 *
 */
public class ManageProjectAction extends ActionSupport
{

    private long id;

    private Project project;

    private ProjectManager projectManager;

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
    
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
        project = projectManager.getProject(id);
        return SUCCESS;
    }
}
