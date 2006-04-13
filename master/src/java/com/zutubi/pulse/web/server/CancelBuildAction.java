/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.server;

import com.zutubi.pulse.FatController;
import com.zutubi.pulse.web.ActionSupport;

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
