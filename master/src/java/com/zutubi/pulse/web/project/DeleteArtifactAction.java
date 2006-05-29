/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.web.ActionSupport;

/**
 */
public class DeleteArtifactAction extends ActionSupport
{
    private long id;
    private long projectId;
    private ProjectManager projectManager;
    private Project project;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public void validate()
    {
        project = projectManager.getProject(projectId);
        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return;
        }

        PulseFileDetails details = project.getPulseFileDetails();
        if(!TemplatePulseFileDetails.class.isAssignableFrom(details.getClass()))
        {
            addActionError("Invalid operation for project");
        }
    }

    public String execute()
    {
        projectManager.deleteArtifact(project, id);
        return SUCCESS;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
