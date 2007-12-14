package com.zutubi.pulse.bootstrap;

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
