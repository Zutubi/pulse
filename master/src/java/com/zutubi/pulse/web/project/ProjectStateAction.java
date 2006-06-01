package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;

/**
 * An action to change the state of a project (e.g. to pause it);
 */
public class ProjectStateAction extends ProjectActionSupport
{
    private long id;
    private boolean pause;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public boolean isPause()
    {
        return pause;
    }

    public void setPause(boolean pause)
    {
        this.pause = pause;
    }

    public String execute() throws Exception
    {
        Project project = getProjectManager().getProject(id);
        if (project == null)
        {
            addActionError("Unknown project [" + id + "]");
            return ERROR;
        }

        if (pause)
        {
            getProjectManager().pauseProject(project);
        }
        else
        {
            getProjectManager().resumeProject(project);
        }

        return SUCCESS;
    }
}
