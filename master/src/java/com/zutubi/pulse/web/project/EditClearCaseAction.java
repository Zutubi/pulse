package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Svn;
import com.zutubi.pulse.model.ClearCase;

/**
 */
public class EditClearCaseAction extends AbstractEditScmAction
{
    private ClearCase scm = new ClearCase();

    public ClearCase getScm()
    {
        return scm;
    }

    public String getScmProperty()
    {
        return "clearCase";
    }

    public ClearCase getClearCase()
    {
        return getScm();
    }

    public void prepare() throws Exception
    {
        super.prepare();
        scm = (ClearCase) getScmManager().getScm(getId());
    }
}
