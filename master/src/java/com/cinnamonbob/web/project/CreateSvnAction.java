package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Scm;
import com.cinnamonbob.model.Svn;

/**
 *
 *
 */
public class CreateSvnAction extends AbstractCreateScmAction
{

    private Svn svn = new Svn();

    public Svn getSvn()
    {
        return svn;
    }

    public Scm getScm()
    {
        return getSvn();
    }

    public String getScmProperty()
    {
        return "svn";
    }

}

