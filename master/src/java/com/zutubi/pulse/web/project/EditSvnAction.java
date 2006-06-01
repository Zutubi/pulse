package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Svn;

/**
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

    public void prepare() throws Exception
    {
        scm = (Svn) getScmManager().getScm(getId());
    }
}
