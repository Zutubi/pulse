package com.cinnamonbob.web.project;

import com.cinnamonbob.model.P4;
import com.cinnamonbob.model.Scm;

/**
 *
 *
 */
public class CreateP4Action extends AbstractCreateScmAction
{
    private P4 p4 = new P4();

    public P4 getP4()
    {
        return p4;
    }

    public Scm getScm()
    {
        return getP4();
    }

    public String getScmProperty()
    {
        return "p4";
    }
}

