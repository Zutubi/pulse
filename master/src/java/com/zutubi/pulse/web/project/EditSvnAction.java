package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.model.Svn;
import com.zutubi.util.StringUtils;

/**
 */
public class EditSvnAction extends AbstractEditScmAction
{
    private Svn scm = new Svn();
    private boolean verifyExternals;

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

    public boolean isVerifyExternals()
    {
        return verifyExternals;
    }

    public void setVerifyExternals(boolean verifyExternals)
    {
        this.verifyExternals = verifyExternals;
    }

    public void prepare() throws Exception
    {
        super.prepare();
        scm = (Svn) getScmManager().getScm(getId());
    }


    public String doInput()
    {
        verifyExternals = scm.getVerifyExternals();
        return super.doInput();
    }

    public void validate()
    {
        super.validate();
        if(hasErrors())
        {
            return;
        }

        try
        {
            if(TextUtils.stringSet(scm.getExternalPaths()))
            {
                StringUtils.split(scm.getExternalPaths());
            }
        }
        catch(IllegalArgumentException e)
        {
            addFieldError("svn.externalPaths", e.getMessage());
        }
    }

    public String execute()
    {
        scm.setVerifyExternals(verifyExternals);
        return super.execute();
    }
}
