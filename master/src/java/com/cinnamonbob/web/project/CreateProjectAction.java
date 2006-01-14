package com.cinnamonbob.web.project;

import com.cinnamonbob.model.CustomBobFileDetails;
import com.cinnamonbob.model.Project;

/**
 * 
 *
 */
public class CreateProjectAction extends ProjectActionSupport
{
    private Project project = new Project();
    private String bobFileName = "bob.xml";

    public Project getProject()
    {
        return project;
    }

    public long getId()
    {
        return getProject().getId();
    }

    public String getBobFileName()
    {
        return bobFileName;
    }

    public void setBobFileName(String bobFileName)
    {
        this.bobFileName = bobFileName;
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
        project.setBobFileDetails(new CustomBobFileDetails(bobFileName));
        getProjectManager().save(project);
        return SUCCESS;
    }
}
