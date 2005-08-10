package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Cvs;
import com.cinnamonbob.model.Project;

/**
 *
 *
 */
public class CreateCvsAction extends ScmActionSupport
{
    private Cvs cvs = new Cvs();
    private long id;

    public Cvs getCvs()
    {
        return cvs;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String execute()
    {
        setCommonFields(cvs);

        Project project = getProjectManager().getProject(id);
        project.addScm(cvs);
        getProjectManager().save(project);
        return SUCCESS;

    }

    public String doDefault()
    {
        return SUCCESS;
    }
}
