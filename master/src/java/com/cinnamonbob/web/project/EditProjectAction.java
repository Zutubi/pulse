package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Project;
import com.opensymphony.xwork.Preparable;

import java.util.Arrays;

/**
 * 
 *
 */
public class EditProjectAction extends ProjectActionSupport implements Preparable
{
    private long id;
    private Project project;// = new Project();

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return this.id;
    }

    public Project getProject()
    {
        return project;
    }

    public boolean checkProject()
    {
        if (project == null)
        {
            addActionError("Unknown project [" + getId() + "]");
            return true;
        }

        return false;
    }

    public void prepare() throws Exception
    {
        project = getProjectManager().getProject(getId());
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        if (checkProject())
        {
            return;
        }

        // check that the requested projects name is not already in use.
        if (getProjectManager().getProject(project.getName()) != null)
        {
            addFieldError("project.name", getText("project.name.exists", Arrays.asList(new String[]{project.getName()})));
        }
    }

    public String doInput()
    {
        if (checkProject())
        {
            return ERROR;
        }

        return INPUT;
    }

    public String execute()
    {
        getProjectManager().save(project);
        return SUCCESS;
    }

}
