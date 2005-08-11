package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Cvs;

/**
 *
 *
 */
public class EditCvsAction extends ProjectActionSupport
{
    private long id;
    private long project;

    private Cvs scm = new Cvs();

    public Cvs getScm()
    {
        return scm;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return this.id;
    }

    public String execute()
    {
        Cvs persistentCvs = (Cvs) getScmManager().getScm(id);
        persistentCvs.setPassword(scm.getPassword());
        persistentCvs.setPath(scm.getPath());
        persistentCvs.setRoot(scm.getRoot());

        return SUCCESS;
    }

    public long getProject()
    {
        return project;
    }

    public void setProject(long project)
    {
        this.project = project;
    }
}
