package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;

/**
 * Abstract base for actions that manipulate project responsibility.
 */
public abstract class ResponsibilityActionBase extends ActionSupport
{
    private long projectId;
    private SimpleResult result;

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    protected Project getProject()
    {
        Project project = projectManager.getProject(projectId, true);
        if (project == null)
        {
            throw new LookupErrorException("Unknown project [" + projectId + "]");
        }

        return project;
    }

    @Override
    public String execute() throws Exception
    {
        try
        {
            result = doExecute();
        }
        catch (Exception e)
        {
            result = new SimpleResult(false, e.getMessage());
        }

        return SUCCESS;
    }

    protected abstract SimpleResult doExecute();
}
