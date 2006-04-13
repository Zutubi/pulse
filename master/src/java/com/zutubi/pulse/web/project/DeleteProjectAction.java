/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;

/**
 * Handles deletion of a project, and all of its build results.
 */
public class DeleteProjectAction extends ProjectActionSupport
{
    private long id;
    private Project project;
    private long totalBuilds;
    private String ok = null;

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

    public void setProject(Project project)
    {
        this.project = project;
    }

    public long getTotalBuilds()
    {
        return totalBuilds;
    }

    public void setOk(String ok)
    {
        this.ok = ok;
    }

    private boolean lookupProject()
    {
        project = getProjectManager().getProject(id);
        if (project == null)
        {
            addActionError("Uknown project [" + id + "]");
            return false;
        }

        return true;
    }

    public String doInput()
    {
        if (!lookupProject())
        {
            return ERROR;
        }

        totalBuilds = getBuildManager().getBuildCount(project, null, null);
        return INPUT;
    }

    public String execute()
    {
        if (!lookupProject())
        {
            return ERROR;
        }

        if (ok != null)
        {
            project = getProjectManager().pauseProject(project.getId());
            if (project.getState() != Project.State.PAUSED)
            {
                addActionError("The project is currently building.  Please wait for the project to pause and try again.");
                return "cancel";
            }

            // We can't use the project entity we have loaded above to delete
            // the project, as the project manager will use a new session.
            getProjectManager().delete(project.getId());
            return SUCCESS;
        }
        else
        {
            return "cancel";
        }
    }
}
