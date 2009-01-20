package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.servercore.ShutdownManager;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.pulse.servercore.jetty.JettyServerManager;

/**
 *
 *
 */
public class RegisterWebserverShutdownStartupTask implements StartupTask
{
    private ShutdownManager shutdownManager;

    public void execute()
    {
        SpringComponentContext.addClassPathContextDefinitions("com/zutubi/pulse/master/bootstrap/context/webserverContext.xml");

        JettyServerManager jettyServerManager = SpringComponentContext.getBean("jettyServerManager");
        shutdownManager.addStoppable(jettyServerManager);
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void setShutdownManager(ShutdownManager shutdownManager)
    {
        this.shutdownManager = shutdownManager;
    }
}
