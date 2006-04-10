package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.P4;

/**
 *
 *
 */
public class EditP4Action extends AbstractEditScmAction
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
