package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * An action for toggling a build's pinned status.
 * */
public class TogglePinAction extends ActionSupport
{
    private long buildId;
    private boolean pin;
    private SimpleResult result;

    private BuildManager buildManager;

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public void setPin(boolean pin)
    {
        this.pin = pin;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    public String execute() throws Exception
    {
        BuildResult buildResult = buildManager.getBuildResult(buildId);
        if (buildResult == null)
        {
            result = new SimpleResult(false, "Unknown build [" + buildId + "]");
        }
        else
        {
            try
            {
                buildManager.togglePin(buildResult, pin);
                result = new SimpleResult(true, "Build " + (pin ? "pinned" : "unpinned") + ".");
            }
            catch (Exception e)
            {
                result = new SimpleResult(false, e.getMessage());
            }
        }

        return SUCCESS;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
