package com.zutubi.pulse.bootstrap.tasks;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import com.zutubi.pulse.bootstrap.StartupTask;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.Version;

import java.text.DateFormat;
import java.util.Date;

/**
 */
public class SystemStartedMessageStartupTask implements StartupTask
{
    private MasterConfigurationManager configurationManager;
    private ConfigurationProvider configurationProvider;

    public void execute()
    {
        // let the user know that the system is now up and running.
        GeneralAdminConfiguration adminConfig = configurationProvider.get(GeneralAdminConfiguration.class);
        SystemConfiguration sysConfig = configurationManager.getSystemConfig();

        //TODO: I18N this message.
        String date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(new Date());
        System.err.printf("[%s] Pulse %s is now available on port %s at context path '%s' [base URL configured as: %s]\n", date, Version.getVersion().getVersionNumber(), sysConfig.getServerPort(), sysConfig.getContextPath(), adminConfig.getBaseUrl());
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
