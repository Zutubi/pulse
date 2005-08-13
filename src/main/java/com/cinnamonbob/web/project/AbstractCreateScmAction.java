package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Scm;
import com.cinnamonbob.model.Project;

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

        // ensure that the name is unique to the project.
        Scm projectsScm = project.getScm(getScm().getName());
        if (projectsScm != null)
        {
            addFieldError(getScmProperty() + ".name", "Name already within this project.");
        }
    }

    public String execute()
    {
        Project project = getProjectManager().getProject(getProject());
        project.addScm(getScm());
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
