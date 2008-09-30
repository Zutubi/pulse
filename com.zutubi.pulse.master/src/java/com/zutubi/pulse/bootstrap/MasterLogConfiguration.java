package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.logging.LogConfiguration;
import com.zutubi.pulse.logging.LogConfigurationManager;
import com.zutubi.pulse.tove.config.admin.LoggingConfiguration;
import com.zutubi.pulse.spring.SpringComponentContext;
import com.zutubi.tove.config.ConfigurationEventListener;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.events.ConfigurationEvent;
import com.zutubi.tove.config.events.PostSaveEvent;

/**
 * Master implementation of the log configuration interface, which is just a
 * thin wrapper around the configuration object.  The minor complication is
 * that this may be accessed *before* the configufation subsystem has
 * started, when we have not yet got a data directory.  In this situation,
 * defaults apply.
 */
public class MasterLogConfiguration implements LogConfiguration, ConfigurationEventListener
{
    private static final String CONFIGURATION_PROVIDER_NAME = "configurationProvider";

    private LoggingConfiguration defaults = new LoggingConfiguration();
    private ConfigurationProvider configurationProvider;
    private LogConfigurationManager logConfigurationManager;

    public String getLoggingLevel()
    {
        return getLoggingConfiguration().getLevel();
    }

    public boolean isEventLoggingEnabled()
    {
        return getLoggingConfiguration().isEventLoggingEnabled();
    }

    private LoggingConfiguration getLoggingConfiguration()
    {
        ConfigurationProvider configurationProvider = getConfigurationProvider();
        if(configurationProvider == null)
        {
            return defaults;
        }
        else
        {
            return configurationProvider.get(LoggingConfiguration.class);
        }
    }

    private ConfigurationProvider getConfigurationProvider()
    {
        if (configurationProvider == null && SpringComponentContext.containsBean(CONFIGURATION_PROVIDER_NAME))
        {
            configurationProvider = SpringComponentContext.getBean(CONFIGURATION_PROVIDER_NAME);
            if (configurationProvider != null)
            {
                configurationProvider.registerEventListener(this, false, false, LoggingConfiguration.class);
            }
        }
        return configurationProvider;
    }

    public void handleConfigurationEvent(ConfigurationEvent event)
    {
        if (event instanceof PostSaveEvent)
        {
            logConfigurationManager.applyConfig();
        }
    }

    public void setLogConfigurationManager(LogConfigurationManager logConfigurationManager)
    {
        this.logConfigurationManager = logConfigurationManager;
    }
}
