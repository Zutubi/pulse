package com.zutubi.pulse.bootstrap.tasks;

import com.zutubi.pulse.jetty.JettyManager;
import com.zutubi.pulse.bootstrap.StartupTask;

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
