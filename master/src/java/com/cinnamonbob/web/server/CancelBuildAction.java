package com.cinnamonbob.web.server;

import com.cinnamonbob.FatController;
import com.cinnamonbob.web.ActionSupport;

/**
 */
public class CancelBuildAction extends ActionSupport
{
    private long buildId;
    private FatController fatController;

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
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
}
