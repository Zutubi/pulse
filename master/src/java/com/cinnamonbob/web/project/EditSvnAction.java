package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Svn;
import com.opensymphony.xwork.Preparable;

/**
 */
public class EditSvnAction extends AbstractEditScmAction implements Preparable
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

    public void prepare() throws Exception
    {
        scm = (Svn) getScmManager().getScm(getId());
    }
}
