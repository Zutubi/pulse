package com.zutubi.pulse.master.bootstrap;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.tove.config.admin.LoggingConfiguration;
import com.zutubi.pulse.servercore.events.system.SystemStartedListener;
import com.zutubi.pulse.servercore.util.logging.LogConfiguration;
import com.zutubi.pulse.servercore.util.logging.LogConfigurationManager;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.TypeAdapter;
import com.zutubi.tove.config.TypeListener;

/**
 * Master implementation of the log configuration interface, which is just a
 * thin wrapper around the configuration object.  The minor complication is
 * that this may be accessed *before* the configuration subsystem has
 * started, when we have not yet got a data directory.  In this situation,
 * defaults apply.
 */
public class MasterLogConfiguration implements LogConfiguration
{
    private final LoggingConfiguration defaults = new LoggingConfiguration();

    private ConfigurationProvider configurationProvider;
    private LogConfigurationManager logConfigurationManager;

    private EventManager eventManager;

    public void init()
    {
        eventManager.register(new SystemStartedListener()
        {
            public void systemStarted()
            {
                // rewire to ensure all of the necessary components are available.
                SpringComponentContext.autowire(MasterLogConfiguration.this);
                MasterLogConfiguration.this.systemStarted();
            }
        });
    }

    private void systemStarted()
    {
        TypeListener<LoggingConfiguration> listener = new TypeAdapter<LoggingConfiguration>(LoggingConfiguration.class)
        {
            public void postSave(LoggingConfiguration instance, boolean nested)
            {
                // notify the log configuration manager of the update.
                logConfigurationManager.applyConfig();
            }
        };
        listener.register(configurationProvider, true);

        // sync the logging with the current configuration.
        logConfigurationManager.applyConfig();
    }

    public String getLoggingLevel()
    {
        return getLoggingConfiguration().getLevel();
    }

    public boolean isEventLoggingEnabled()
    {
        return getLoggingConfiguration().isEventLoggingEnabled();
    }

    public boolean isConfigAuditLoggingEnabled()
    {
        return getLoggingConfiguration().isConfigAuditLoggingEnabled();
    }

    private LoggingConfiguration getLoggingConfiguration()
    {
        if(configurationProvider == null)
        {
            return defaults;
        }
        else
        {
            return configurationProvider.get(LoggingConfiguration.class);
        }
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setLogConfigurationManager(LogConfigurationManager logConfigurationManager)
    {
        this.logConfigurationManager = logConfigurationManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
