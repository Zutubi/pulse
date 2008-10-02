package com.zutubi.pulse.master.events.system;

import com.zutubi.pulse.servercore.events.system.SystemEvent;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * An event raised when the configuration system is intialised to the point
 * where configuration listeners can be registered.  This is the time to
 * register listeners that must know of all changes (i.e. no changes should
 * happen prior to this event).  Changes <strong>must not</strong> be written
 * to the configuration system before this event is raised; and indeed not
 * during handlers for this event (because other handlers have not had a
 * chance to run).  Changes must wait for the
 * {@link ConfigurationSystemStartedEvent} to fire first.
 *
 * @see com.zutubi.pulse.master.events.system.ConfigurationSystemStartedEvent
 */
public class ConfigurationEventSystemStartedEvent extends SystemEvent
{
    private ConfigurationProvider configurationProvider;

    public ConfigurationEventSystemStartedEvent(ConfigurationProvider configurationProvider)
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
        return "Configuration Event System Started Event";
    }
}
