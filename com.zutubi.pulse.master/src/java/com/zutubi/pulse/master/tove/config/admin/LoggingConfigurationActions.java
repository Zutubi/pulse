package com.zutubi.pulse.master.tove.config.admin;

import com.zutubi.pulse.servercore.util.logging.LogConfigurationManager;

/**
 * The actions object for the logging configuration.
 */
public class LoggingConfigurationActions
{
    private LogConfigurationManager logConfigurationManager;

    /**
     * Trigger a reload of the logging configurations object, allowing people to
     * make external changes to the logging.properties file and have pulse pick
     * up those changes without the need for restarting Pulse.
     *
     * @param config instance
     */
    public void doReload(LoggingConfiguration config)
    {
        logConfigurationManager.reset();
    }

    public void setLogConfigurationManager(LogConfigurationManager logConfigurationManager)
    {
        this.logConfigurationManager = logConfigurationManager;
    }
}
