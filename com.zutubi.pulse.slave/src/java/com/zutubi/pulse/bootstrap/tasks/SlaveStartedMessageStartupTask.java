package com.zutubi.pulse.bootstrap.tasks;

import com.zutubi.pulse.bootstrap.StartupTask;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.Version;

import java.text.DateFormat;
import java.util.Date;

/**
 */
public class SlaveStartedMessageStartupTask implements StartupTask
{
    private ConfigurationManager configurationManager;

    public void execute()
    {
        SystemConfiguration sysConfig = configurationManager.getSystemConfig();
        String date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(new Date());
        System.err.format("[%s] Pulse agent %s is now listening on port %d\n", date, Version.getVersion().getVersionNumber(), sysConfig.getServerPort());
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
