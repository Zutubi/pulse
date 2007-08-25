package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;

public class ForceCleanBuildAction extends ProjectActionSupport
{
    private long id;

    public long getId()
    {
        return id;
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
        Project project = getProjectManager().getProject(projectId, false);

        if (project == null)
        {
            addActionError("Unknown project [" + projectId + "]");
            return ERROR;
        }

        getProjectManager().checkWrite(project);

        project.setForceClean(true);
        getProjectManager().save(project);

        return SUCCESS;
    }
}
