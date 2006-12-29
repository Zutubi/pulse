package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.jetty.JettyManager;

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
