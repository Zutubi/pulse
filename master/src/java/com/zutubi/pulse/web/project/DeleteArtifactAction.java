package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.model.TemplatePulseFileDetails;
import com.zutubi.pulse.web.ActionSupport;

/**
 */
public class DeleteArtifactAction extends ActionSupport
{
    private long id;
    private long projectId;
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
    }

    public String execute()
    {
        projectManager.deleteArtifact(project, id);
        return SUCCESS;
    }

}
