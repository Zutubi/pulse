package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;

/**
 */
public class DeployMainWebappStartupTask implements StartupTask
{
    private WebManager webManager;

    public void execute()
    {
        webManager.deployMain();
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void setWebManager(WebManager webManager)
    {
        this.webManager = webManager;
    }
}
