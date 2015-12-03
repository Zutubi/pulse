package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.master.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 */
public class SystemStartedMessageStartupTask implements StartupTask
{
    private MasterConfigurationManager configurationManager;
    private ConfigurationProvider configurationProvider;

    public void execute()
    {
        // let the user know that the system is now up and running.
        GlobalConfiguration globalConfig = configurationProvider.get(GlobalConfiguration.class);
        SystemConfiguration sysConfig = configurationManager.getSystemConfig();

        //TODO: I18N this message.
        System.err.printf(DefaultSetupManager.dateStamp() + "Pulse %s is now available on port %s at context path '%s' [base URL configured as: %s]\n", Version.getVersion().getVersionNumber(), sysConfig.getServerPort(), sysConfig.getContextPath(), globalConfig.getBaseUrl());
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
