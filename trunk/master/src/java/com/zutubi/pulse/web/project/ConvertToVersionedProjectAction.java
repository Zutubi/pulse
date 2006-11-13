package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.VersionedPulseFileDetails;

/**
 */
public class ConvertToVersionedProjectAction extends ProjectActionSupport
{
    private long id;
    private VersionedPulseFileDetails details = new VersionedPulseFileDetails();
    private Project project;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public VersionedPulseFileDetails getDetails()
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
