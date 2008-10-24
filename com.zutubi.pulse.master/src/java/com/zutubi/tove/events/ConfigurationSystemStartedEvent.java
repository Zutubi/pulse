package com.zutubi.tove.events;

import com.zutubi.tove.config.ConfigurationProvider;

/**
 * An event raised when the configuration system is completely available.
 * Handlers are free to trigger changes to the configuration upon receipt of
 * this event.  Note that to register configuration listeners, you should
 * handle the {@link ConfigurationEventSystemStartedEvent} instead - changes
 * may occur before your handler is called for this event.
 *
 * @see ConfigurationEventSystemStartedEvent
 */
public class ConfigurationSystemStartedEvent extends ConfigurationSystemEvent
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
