package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.CustomPulseFileDetails;
import com.zutubi.pulse.model.Project;

/**
 */
public class ConvertToCustomProjectAction extends ProjectActionSupport
{
    private long id;
    private CustomPulseFileDetails details = new CustomPulseFileDetails();
    private Project project;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public CustomPulseFileDetails getDetails()
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
        project.setPulseFileDetails(details);
        getProjectManager().save(project);

        return SUCCESS;
    }
}
