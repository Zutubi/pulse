package com.zutubi.pulse.slave.bootstrap.tasks;

import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;

public class SlaveSystemPropertiesStartupTask implements StartupTask
{
    private ConfigurationManager configurationManager;

    public void execute()
    {
        configurationManager.loadSystemProperties();
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
