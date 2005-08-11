package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Svn;

/**
 *
 *
 */
public class EditSvnAction extends ProjectActionSupport
{
    private long id;
    private long project;

    private Svn scm = new Svn();

    public Svn getScm()
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
        Svn persistentSvn = (Svn) getScmManager().getScm(id);
        persistentSvn.setKeyfile(scm.getKeyfile());
        persistentSvn.setPassphrase(scm.getPassphrase());
        persistentSvn.setPassword(scm.getPassword());
        persistentSvn.setPath(scm.getPath());
        persistentSvn.setUrl(scm.getUrl());
        persistentSvn.setUsername(scm.getUsername());

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
