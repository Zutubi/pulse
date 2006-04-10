package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.*;

/**
 *
 *
 */
public class EditScmAction extends ProjectActionSupport
{
    private long id;
    private long projectId;
    private Project project;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public Project getProject()
    {
        return project;
    }

    public String doInput()
    {
        project = getProjectManager().getProject(projectId);

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

        addActionError("Internal error: unrecognised scm type");
        return ERROR;
    }
}
