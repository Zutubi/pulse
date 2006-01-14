package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Svn;

/**
 *
 *
 */
public class EditSvnAction extends AbstractEditScmAction
{
    private Svn scm = new Svn();

    public Svn getScm()
    {
        return scm;
    }

    public String getScmProperty()
    {
        return "svn";
    }

    public Svn getSvn()
    {
        return getScm();
    }

    public String doDefault()
    {
        scm = (Svn) getScmManager().getScm(getId());
        return SUCCESS;
    }

    public String execute()
    {
        Svn persistentSvn = (Svn) getScmManager().getScm(getId());
        persistentSvn.setKeyfile(scm.getKeyfile());
        persistentSvn.setPassphrase(scm.getPassphrase());
        persistentSvn.setPassword(scm.getPassword());
        persistentSvn.setPath(scm.getPath());
        persistentSvn.setUrl(scm.getUrl());
        persistentSvn.setUsername(scm.getUsername());

        return SUCCESS;
    }
}
