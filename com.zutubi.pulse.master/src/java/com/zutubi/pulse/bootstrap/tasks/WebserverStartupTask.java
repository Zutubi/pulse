package com.zutubi.pulse.bootstrap.tasks;

import com.zutubi.pulse.bootstrap.StartupTask;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.jetty.JettyManager;
import com.zutubi.pulse.ShutdownManager;

/**
 *
 *
 */
public class WebserverStartupTask implements StartupTask
{
    private ShutdownManager shutdownManager;

    public void execute()
    {
        SpringComponentContext.addClassPathContextDefinitions("com/zutubi/pulse/bootstrap/context/webserverContext.xml");

        JettyManager jettyManager = SpringComponentContext.getBean("jettyManager");
        shutdownManager.addStoppable(jettyManager);
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
