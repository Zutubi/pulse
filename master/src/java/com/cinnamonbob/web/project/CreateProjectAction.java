package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Project;

/**
 * 
 *
 */
public class CreateProjectAction extends ProjectActionSupport
{
    private Project project = new Project();

    public Project getProject()
    {
        return project;
    }

    public long getId()
    {
        return getProject().getId();
    }
    
    public void validate()
    {
        if (hasErrors())
        {
            // do not attempt to validate unless all other validation rules have 
            // completed successfully.
            return;
        }

        if (getProjectManager().getProject(project.getName()) != null)
        {
            // login name already in use.
            addFieldError("project.name", "Project name " + project.getName() + " is already being used.");
        }
    }

    public String execute()
    {
        getProjectManager().save(project);
        return SUCCESS;
    }

}
