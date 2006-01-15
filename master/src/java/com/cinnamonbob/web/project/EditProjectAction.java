package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Project;

/**
 * 
 *
 */
public class EditProjectAction extends ProjectActionSupport
{
    private long id;

    private Project project = new Project();

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

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        if (getProjectManager().getProject(getId()) == null)
        {
            addActionError("Unknown project [" + getId() + "]");
        }
    }

    public String doDefault()
    {
        project = getProjectManager().getProject(getId());
        return SUCCESS;
    }

    public String execute()
    {
        Project persistentProject = getProjectManager().getProject(getId());
        persistentProject.setName(project.getName());
        persistentProject.setDescription(project.getDescription());

        return SUCCESS;
    }
}
