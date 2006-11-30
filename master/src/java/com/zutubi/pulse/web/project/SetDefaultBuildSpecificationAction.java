package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 *
 *
 */
public class SetDefaultBuildSpecificationAction extends ActionSupport
{
    private long id;
    private long projectId;

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

    public String execute()
    {
        Project project = projectManager.getProject(projectId);
        if (project == null)
        {
            addActionError("Unknown project [" + id + "]");
            return ERROR;
        }

        BuildSpecification specification = project.getBuildSpecification(id);
        if (specification == null)
        {
            addActionError("Unknown specification [" + id + "]");
            return ERROR;
        }

        projectManager.setDefaultBuildSpecification(project, id);
        return SUCCESS;
    }
}
