package com.cinnamonbob.web.project;

import com.cinnamonbob.model.P4;
import com.opensymphony.xwork.Preparable;

/**
 *
 *
 */
public class EditP4Action extends AbstractEditScmAction implements Preparable
{
    private P4 scm = new P4();

    public P4 getScm()
    {
        return scm;
    }

    public String getScmProperty()
    {
        return "p4";
    }

    public P4 getP4()
    {
        return getScm();
    }

    public void prepare() throws Exception
    {
        scm = (P4) getScmManager().getScm(getId());
    }
}
