package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.Scm;

/**
 *
 *
 */
public abstract class AbstractEditScmAction extends ProjectActionSupport
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

    public Project getProject()
    {
        return project;
    }

    public String doInput()
    {
        project = getProjectManager().getProject(projectId);
        return INPUT;
    }

    public String execute()
    {
        getScmManager().save(getScm());
        return SUCCESS;
    }

    public abstract Scm getScm();

    public abstract String getScmProperty();

}
