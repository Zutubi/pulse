package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Cvs;
import com.cinnamonbob.model.Scm;

/**
 *
 *
 */
public class CreateCvsAction extends AbstractCreateScmAction
{
    private Cvs cvs = new Cvs();

    public Cvs getCvs()
    {
        return cvs;
    }

    public Scm getScm()
    {
        return getCvs();
    }

    public String getScmProperty()
    {
        return "cvs";
    }
}
