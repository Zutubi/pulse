package com.cinnamonbob.web;

import com.cinnamonbob.model.Svn;
import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.model.Project;
import com.opensymphony.xwork.ActionSupport;

/**
 * 
 *
 */
public class CreateScmAction extends ActionSupport
{
    private long id;
    private Svn scm = new Svn();

    private ProjectManager projectManager;

    public void setProjectManager(ProjectManager manager)
    {
        projectManager = manager;
    }

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

        if (projectManager.getProject(id) == null)
        {
            addActionError("No project with id '" + Long.toString(id) + "'");
        }
    }
    
    public String execute()
    {
        Project project = projectManager.getProject(id);
        project.addScm(scm);
        projectManager.save(project);
        return SUCCESS;
    }
    
    public String doDefault()
    {
        return SUCCESS;
    }
}
