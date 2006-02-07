package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Cvs;
import com.opensymphony.xwork.Preparable;

/**
 *
 *
 */
public class EditCvsAction extends AbstractEditScmAction implements Preparable
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

    public void prepare() throws Exception
    {
        scm = (Cvs) getScmManager().getScm(getId());
    }
}
