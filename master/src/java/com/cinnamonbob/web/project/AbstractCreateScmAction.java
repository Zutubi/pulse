package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.Scm;

/**
 *
 *
 */
public abstract class AbstractCreateScmAction extends ProjectActionSupport
{
    private long project;

    public long getProject()
    {
        return project;
    }

    public void setProject(long project)
    {
        this.project = project;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        // ensure the project id is valid.
        Project project = getProjectManager().getProject(this.project);
        if (project == null)
        {
            addActionError("No project with id '" + Long.toString(this.project) + "'");
            return;
        }
    }

    public String execute()
    {
        Project project = getProjectManager().getProject(getProject());
        project.setScm(getScm());
        getProjectManager().save(project);
        return SUCCESS;
    }

    public String doDefault()
    {
        return SUCCESS;
    }

    public abstract Scm getScm();

    public abstract String getScmProperty();

}
