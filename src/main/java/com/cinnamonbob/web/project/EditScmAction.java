package com.cinnamonbob.web.project;

import com.cinnamonbob.model.*;

/**
 *
 *
 */
public class EditScmAction extends ProjectActionSupport
{
    private long id;

    private String type;

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
        Scm scm = getScmManager().getScm(id);
        if (scm == null)
        {
            return INPUT;
        }
        
        if (scm instanceof P4)
        {
            return  "p4";
        }
        else if (scm instanceof Cvs)
        {
            return "cvs";
        }
        else if (scm instanceof Svn)
        {
            return "svn";
        }
        return ERROR;
    }

    public String execute()
    {
        return SUCCESS;
    }
}
