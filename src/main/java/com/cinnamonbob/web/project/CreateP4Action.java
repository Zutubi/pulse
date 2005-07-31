package com.cinnamonbob.web.project;

import com.cinnamonbob.model.P4;
import com.cinnamonbob.model.Project;

/**
 *
 *
 */
public class CreateP4Action extends ScmActionSupport
{
    private long id;
    private P4 p4 = new P4();

    public P4 getP4()
    {
        return p4;
    }

    public void setId(long id)
    {
        // TODO: move up the inheritance chain somewhere
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
        setCommonFields(p4);
        Project project = getProjectManager().getProject(id);
        project.addScm(p4);
        getProjectManager().save(project);
        return SUCCESS;
    }

    public String doDefault()
    {
        return SUCCESS;
    }
}

