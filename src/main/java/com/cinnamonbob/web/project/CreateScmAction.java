package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.model.Svn;

/**
 * 
 *
 */
public class CreateScmAction extends BaseProjectAction
{
    private long id;
    private Svn scm = new Svn();

    public Svn getSvn()
    {
        return scm;
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
        project.addScm(scm);
        getProjectManager().save(project);
        return SUCCESS;
    }

    public String doDefault()
    {
        return SUCCESS;
    }
}
