package com.zutubi.pulse.bootstrap.tasks;

import com.zutubi.pulse.bootstrap.StartupTask;
import com.zutubi.pulse.bootstrap.WebManager;
import com.zutubi.pulse.bootstrap.ComponentContext;

/**
 */
public class DeployMainWebappStartupTask implements StartupTask
{
    public void execute()
    {
        WebManager webManager = (WebManager) ComponentContext.getBean("webManager");
        // ii) time to deploy the main application.
        webManager.deployMain();
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
