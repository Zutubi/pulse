package com.zutubi.pulse.events.system;

import com.zutubi.prototype.config.ConfigurationProvider;

/**
 * An event raised when the configuration system is completely available.
 * Handlers are free to trigger changes to the configuration upon receipt of
 * this event.  Note that to register configuration listeners, you should
 * handle the {@link ConfigurationEventSystemStartedEvent} instead - changes
 * may occur before your handler is called for this event.
 *
 * @see com.zutubi.pulse.events.system.ConfigurationEventSystemStartedEvent
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
