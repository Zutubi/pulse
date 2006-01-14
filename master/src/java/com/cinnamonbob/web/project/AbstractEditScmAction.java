package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Scm;

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

    public abstract Scm getScm();

    public abstract String getScmProperty();
}
