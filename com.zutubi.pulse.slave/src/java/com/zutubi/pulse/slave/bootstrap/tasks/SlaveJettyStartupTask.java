package com.zutubi.pulse.slave.bootstrap.tasks;

import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.pulse.servercore.jetty.JettyManager;

/**
 */
public class SlaveJettyStartupTask implements StartupTask
{
    private JettyManager jettyManager;

    public void execute()
    {
        jettyManager.start();
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void setJettyManager(JettyManager jettyManager)
    {
        this.jettyManager = jettyManager;
    }
}
