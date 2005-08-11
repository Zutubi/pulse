package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Svn;
import com.cinnamonbob.model.Project;

/**
 *
 *
 */
public class CreateSvnAction extends ScmActionSupport
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
        return id;
    }

    public void validate()
    {
        if (hasErrors())
        {
            // do not attempt to validate unless all other validation rules have
            // completed successfully.
            return;
        }

        if (getProjectManager().getProject(id) == null)
        {
            addActionError("No project with id '" + Long.toString(id) + "'");
        }
    }

    public String execute()
    {
        Project project = getProjectManager().getProject(id);
        project.addScm(svn);
        getProjectManager().save(project);
        return SUCCESS;
    }

    public String doDefault()
    {
        return SUCCESS;
    }
}

