package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Cvs;

/**
 *
 *
 */
public class EditCvsAction extends AbstractEditScmAction
{

    private Cvs scm = new Cvs();

    public Cvs getScm()
    {
        return scm;
    }

    public String getScmProperty()
    {
        return "cvs";
    }

    public Cvs getCvs()
    {
        return getScm();
    }

    public String doDefault()
    {
        scm = (Cvs) getScmManager().getScm(getId());
        return SUCCESS;
    }

    public String execute()
    {
        Cvs persistentCvs = (Cvs) getScmManager().getScm(getId());
        persistentCvs.setName(scm.getName());
        persistentCvs.setPassword(scm.getPassword());
        persistentCvs.setPath(scm.getPath());
        persistentCvs.setRoot(scm.getRoot());
        return SUCCESS;
    }
}
