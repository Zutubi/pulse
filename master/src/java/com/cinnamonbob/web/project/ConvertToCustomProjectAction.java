package com.cinnamonbob.web.project;

import com.cinnamonbob.model.CustomBobFileDetails;
import com.cinnamonbob.model.Project;

/**
 */
public class ConvertToCustomProjectAction extends ProjectActionSupport
{
    private long id;
    private CustomBobFileDetails details = new CustomBobFileDetails();
    private Project project;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public CustomBobFileDetails getDetails()
    {
        return details;
    }

    public Project getProject()
    {
        return project;
    }

    public String doInput()
    {
        project = lookupProject(id);
        if(project == null)
        {
            return ERROR;
        }

        return INPUT;
    }

    public void validate()
    {
        project = lookupProject(id);
    }

    public String execute()
    {
        project.setBobFileDetails(details);
        getProjectManager().save(project);

        return SUCCESS;
    }
}
