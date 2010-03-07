package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.servercore.ShutdownManager;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.pulse.servercore.jetty.JettyManager;

/**
 *
 *
 */
public class WebserverStartupTask implements StartupTask
{
    private ShutdownManager shutdownManager;

    public void execute()
    {
        SpringComponentContext.addClassPathContextDefinitions("com/zutubi/pulse/master/bootstrap/context/webserverContext.xml");

        JettyManager jettyManager = SpringComponentContext.getBean("jettyManager");
        shutdownManager.addStoppable(jettyManager);
        jettyManager.start();
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
