package com.cinnamonbob.web;

import com.opensymphony.xwork.ActionSupport;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.xwork.interceptor.Cancelable;

/**
 * 
 *
 */
public class CreateProjectAction extends ActionSupport implements Cancelable
{
    private String cancel = null;
    
    private Project project = new Project();

    private ProjectManager projectManager;

    public void setProjectManager(ProjectManager manager)
    {
        projectManager = manager;
    }

    public void setCancel(String name)
    {
        cancel = name;
    }

    public boolean isCancelled()
    {
        return cancel != null;
    }

    public Project getProject()
    {
        return project;
    }

    public void validate()
    {
        if (hasErrors())
        {
            // do not attempt to validate unless all other validation rules have 
            // completed successfully.
            return;
        }

        if (projectManager.getProject(project.getName()) != null)
        {
            // login name already in use.
            addFieldError("project.name", "Project name " + project.getName() + " is already being used.");
        }
    }

    public String execute()
    {
        projectManager.save(project);
        return SUCCESS;
    }

}
