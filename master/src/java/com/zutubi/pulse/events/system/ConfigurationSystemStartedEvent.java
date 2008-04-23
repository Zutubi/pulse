package com.zutubi.pulse.events.system;

import com.zutubi.prototype.config.ConfigurationProvider;

/**
 * An event raised when the configuration system is completely available.
 */
public class ConfigurationSystemStartedEvent extends SystemEvent
{
    private ConfigurationProvider configurationProvider;

    public ConfigurationSystemStartedEvent(ConfigurationProvider configurationProvider)
    {
        super(configurationProvider);
        this.configurationProvider = configurationProvider;
    }

    public ConfigurationProvider getConfigurationProvider()
    {
        return configurationProvider;
    }

    public String toString()
    {
        return "Configuration System Started Event";
    }
}
