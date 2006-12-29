package com.zutubi.pulse.bootstrap;

/**
 */
public class SlaveStartedMessageStartupTask implements StartupTask
{
    private ConfigurationManager configurationManager;

    public void execute()
    {
        System.out.println("The agent is now listening on port: " + configurationManager.getSystemConfig().getServerPort());
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
