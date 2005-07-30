package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.Scm;

/**
 *
 *
 */
public class DeleteScmAction extends ProjectActionSupport
{
    private long id;
    private String name;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void validate()
    {
        Project project = getProjectManager().getProject(id);
        if (project == null)
        {
            addFieldError("id", "Unknown project["+id+"]");
        }
    }

    public String execute()
    {
        Project project = getProjectManager().getProject(id);
        Scm scm = project.getScm(name);
        if (scm != null)
        {
            project.remove(scm);
        }
        return SUCCESS;
    }
}
