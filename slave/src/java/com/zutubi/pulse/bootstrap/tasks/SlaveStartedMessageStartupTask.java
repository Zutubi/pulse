package com.zutubi.pulse.bootstrap.tasks;

import com.zutubi.pulse.bootstrap.StartupTask;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.SystemConfiguration;

/**
 */
public class SlaveStartedMessageStartupTask implements StartupTask
{
    private ConfigurationManager configurationManager;

    public void execute()
    {
        SystemConfiguration sysConfig = configurationManager.getSystemConfig();
        String str = "The agent is now listening on port: %s";
        String msg = String.format(str, sysConfig.getServerPort());
        System.err.println(msg);
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
