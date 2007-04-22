package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;

/**
 */
public class BrowseProjectScmAction extends AbstractBrowseDirAction
{
    private long id;
    private Project project;

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

    public boolean getShowSizes()
    {
        return false;
    }

    public String execute()
    {
        project = getProjectManager().getProject(id);
        if (project == null)
        {
            addActionError("Unknown project [" + id + "]");
            return ERROR;
        }

        getProjectManager().checkWrite(project);

        try
        {
            return getPath().length() > 0 ? "file" : SUCCESS;
        }
        catch (Exception e)
        {
            addActionError("Error browsing SCM: " + e.getMessage());
            return ERROR;
        }
    }
}
