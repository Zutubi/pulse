package com.zutubi.pulse.web.server;

import com.zutubi.pulse.FatController;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.webwork.mapping.Urls;

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
        Urls urls = new Urls("");
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
        fatController.terminateBuild(buildId, false);
        Thread.sleep(500);
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
