package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;

/**
 */
public class DeployMainWebappStartupTask implements StartupTask
{
    public void execute()
    {
        WebManager webManager = (WebManager) SpringComponentContext.getBean("webManager");
        // ii) time to deploy the main application.
        webManager.deployMain();
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
