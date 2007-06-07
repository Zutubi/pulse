package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.pulse.events.Event;

/**
 * Base for events raised when configuration changes occur.
 */
public class ConfigurationEvent extends Event<ConfigurationTemplateManager>
{
    private String path;

    public ConfigurationEvent(ConfigurationTemplateManager source, String path)
    {
        super(source);
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }
}
