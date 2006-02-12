package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Svn;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.Preparable;

/**
 */
public class EditSvnAction extends AbstractEditScmAction implements Preparable
{
    private Svn scm = new Svn();

    public Svn getScm()
    {
        if (!TextUtils.stringSet(scm.getPassword()))
        {
            scm.setPassword(null);
        }

        if (!TextUtils.stringSet(scm.getKeyfile()))
        {
            scm.setKeyfile(null);
        }

        if (!TextUtils.stringSet(scm.getPassphrase()))
        {
            scm.setPassphrase(null);
        }

        if (!TextUtils.stringSet(scm.getPath()))
        {
            scm.setPath(null);
        }

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
