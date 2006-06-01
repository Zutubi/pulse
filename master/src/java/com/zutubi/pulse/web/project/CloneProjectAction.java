package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;

/**
 * Clones an existing project, creating a new project that is identical
 * apart from the name and description.
 */
public class CloneProjectAction extends ProjectActionSupport
{
    private long id;
    private Project project;
    private String name;
    private String description;
    private long cloneId;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Project getProject()
    {
        return project;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public long getCloneId()
    {
        return cloneId;
    }

    public String doInput()
    {
        project = lookupProject(id);
        if(hasErrors())
        {
            return ERROR;
        }

        return INPUT;
    }

    public void validate()
    {
        project = lookupProject(id);
        if(hasErrors())
        {
            return;
        }

        // Check the new name is not in use
        if(getProjectManager().getProject(name) != null)
        {
            addFieldError("name", "The name '" + name + "' is already in use.");
        }
    }

    public String execute()
    {
        Project clone = getProjectManager().cloneProject(project, name, description);
        cloneId = clone.getId();
        return SUCCESS;
    }
}
