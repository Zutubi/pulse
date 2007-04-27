package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.logging.LogConfiguration;
import com.zutubi.pulse.logging.LogConfigurationManager;
import com.zutubi.pulse.prototype.config.admin.LoggingConfiguration;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.ConfigurationEventListener;
import com.zutubi.prototype.config.events.ConfigurationEvent;
import com.zutubi.prototype.config.events.PostSaveEvent;

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
        if(configurationProvider == null && ComponentContext.containsBean(CONFIGURATION_PROVIDER_NAME))
        {
            configurationProvider = ComponentContext.getBean(CONFIGURATION_PROVIDER_NAME);
            if(configurationProvider != null)
            {
                configurationProvider.registerEventListener(this, false, false, LoggingConfiguration.class);
            }
        }
        return configurationProvider;
    }

    public void handleConfigurationEvent(ConfigurationEvent event)
    {
        if(event instanceof PostSaveEvent)
        {
            logConfigurationManager.applyConfig();
        }
    }

    public void setLogConfigurationManager(LogConfigurationManager logConfigurationManager)
    {
        this.logConfigurationManager = logConfigurationManager;
    }
}
