package com.zutubi.pulse.master.xwork.actions.server;

import com.zutubi.pulse.master.build.queue.FatController;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 */
public class CancelBuildAction extends ActionSupport
{
    private long buildId;
    private boolean fromBuild = false;
    private FatController fatController;
    private BuildManager buildManager;

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public void setFromBuild(boolean fromBuild)
    {
        this.fromBuild = fromBuild;
    }

    public String getRedirect()
    {
        Urls urls = Urls.getBaselessInstance();
        if(fromBuild)
        {
            BuildResult build = buildManager.getBuildResult(buildId);
            if (build == null)
            {
                return urls.browse();
            }
            else
            {
                return urls.build(build);
            }
        }
        else
        {
            return urls.serverActivity();
        }
    }
    
    public String execute() throws Exception
    {
        BuildResult build = buildManager.getBuildResult(buildId);
        if (build == null)
        {
            throw new IllegalArgumentException("Unknown build '" + buildId + "'");
        }

        String user = getPrinciple();
        fatController.terminateBuild(build, user == null ? null : "requested by '" + user + "'");
        pauseForDramaticEffect();
        return SUCCESS;
    }

    public void setFatController(FatController fatController)
    {
        this.fatController = fatController;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
