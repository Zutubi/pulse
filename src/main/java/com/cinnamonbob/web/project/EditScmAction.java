package com.cinnamonbob.web.project;

import com.cinnamonbob.model.*;

/**
 *
 *
 */
public class EditScmAction extends ProjectActionSupport
{
    private long id;
    private long project;

    private String type;

    private Scm scm;

    public Scm getScm()
    {
        return scm;
    }

    public String getType()
    {
        return type;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String doDefault()
    {
        scm = getScmManager().getScm(id);
        if (scm instanceof P4)
        {
            type =  "p4";
        }
        else if (scm instanceof Cvs)
        {
            type =  "cvs";
        }
        else if (scm instanceof Svn)
        {
            type =  "svn";
        }
        return SUCCESS;
    }

    public String execute()
    {
        return SUCCESS;
    }

    public long getProject()
    {
        return project;
    }

    public void setProject(long project)
    {
        this.project = project;
    }
}
