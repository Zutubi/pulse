package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Svn;

/**
 *
 *
 */
public class EditSvnAction extends ProjectActionSupport
{

    private long id;

    private Svn svn = new Svn();

    public Svn getSvn()
    {
        return svn;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return this.id;
    }

    public String doDefault()
    {
        return "SUCCESS";
    }

    public String execute()
    {

        return "SUCCESS";
    }
}
