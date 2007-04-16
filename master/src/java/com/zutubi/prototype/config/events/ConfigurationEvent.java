package com.zutubi.prototype.config.events;

import com.zutubi.pulse.events.Event;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 * Base for events raised when configuration changes occur.
 */
public class ConfigurationEvent extends Event<ConfigurationPersistenceManager>
{
    private String path;

    public ConfigurationEvent(ConfigurationPersistenceManager source, String path)
    {
        super(source);
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }
}
