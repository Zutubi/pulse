package com.zutubi.pulse.bootstrap.tasks;

import com.zutubi.pulse.bootstrap.StartupTask;
import com.zutubi.pulse.bootstrap.WebManager;
import com.zutubi.pulse.core.spring.SpringComponentContext;

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
