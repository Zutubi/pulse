package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;

/**
 * Abstract base for actions that manipulate build responsibility.
 */
public abstract class ResponsibilityActionBase extends ActionSupport
{
    private long buildId;
    private SimpleResult result;
    protected BuildManager buildManager;

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    protected BuildResult getBuildResult()
    {
        BuildResult buildResult = buildManager.getBuildResult(buildId);
        if (buildResult == null)
        {
            throw new LookupErrorException("Unknown build result [" + buildId + "]");
        }

        if (buildResult.isPersonal())
        {
            throw new IllegalArgumentException("Cannot change responsibility for a personal build");
        }
        return buildResult;
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

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
