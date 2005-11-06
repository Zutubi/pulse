package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Scm;
import com.cinnamonbob.model.Project;

/**
 *
 *
 */
public abstract class AbstractEditScmAction extends ProjectActionSupport
{
    private long id;
    private long project;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

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

        // ensure that the name is unique to the project.
        Project project = getProjectManager().getProject(this.project);
        Scm projectsScm = project.getScm(getScm().getName());
        if (projectsScm != null && projectsScm.getId() != id)
        {
            addFieldError(getScmProperty() + ".name", "Name already within this project.");
        }
    }

    public abstract Scm getScm();

    public abstract String getScmProperty();
}
