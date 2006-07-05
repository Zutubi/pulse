package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 */
public class DeleteBuildAction extends ActionSupport
{
    private long id;
    private BuildManager buildManager;
    private ProjectManager projectManager;
    private BuildResult result;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public BuildResult getResult()
    {
        return result;
    }

    public String execute()
    {
        result = buildManager.getBuildResult(id);
        if (result == null)
        {
            addActionError("Unknown build [" + id + "]");
            return ERROR;
        }

        if(!result.completed())
        {
            addActionError("Build cannot be deleted as it is not complete.");
            return ERROR;
        }

        projectManager.checkWrite(result.getProject());
        buildManager.delete(result);
        return SUCCESS;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
