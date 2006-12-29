package com.zutubi.pulse.bootstrap;

/**
 */
public class SystemStartedMessageStartupTask implements StartupTask
{
    private MasterConfigurationManager configurationManager;

    public void execute()
    {
        // let the user know that the system is now up and running.
        MasterConfiguration appConfig = configurationManager.getAppConfig();
        SystemConfiguration sysConfig = configurationManager.getSystemConfig();

        //TODO: I18N this message.
        String str = "The server is now available on port %s at context path '%s' [base URL configured as: %s]";
        String msg = String.format(str, sysConfig.getServerPort(), sysConfig.getContextPath(), appConfig.getBaseUrl());
        System.err.println(msg);
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
