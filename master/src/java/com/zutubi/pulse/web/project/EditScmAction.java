package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.*;

/**
 *
 *
 */
public class EditScmAction extends ProjectActionSupport
{
    private long id;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String doInput()
    {
        Scm scm = getScmManager().getScm(id);
        if (scm == null)
        {
            addActionError("Unknown scm [" + id + "]");
            return ERROR;
        }

        if (scm instanceof P4)
        {
            return "p4";
        }
        else if (scm instanceof Cvs)
        {
            return "cvs";
        }
        else if (scm instanceof Svn)
        {
            return "svn";
        }
        else if (scm instanceof ClearCase)
        {
            return "clearcase";
        }

        addActionError("Internal error: unrecognised scm type");
        return ERROR;
    }
}
