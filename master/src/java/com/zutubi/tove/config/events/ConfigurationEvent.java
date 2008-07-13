package com.zutubi.tove.config.events;

import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.events.Event;
import com.zutubi.tove.config.ConfigurationTemplateManager;

/**
 * Base for events raised when configuration changes occur.
 */
public abstract class ConfigurationEvent extends Event<ConfigurationTemplateManager>
{
    private Configuration instance;

    public ConfigurationEvent(ConfigurationTemplateManager source, Configuration instance)
    {
        super(source);
        this.instance = instance;
    }

    public Configuration getInstance()
    {
        return instance;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ConfigurationEvent event = (ConfigurationEvent) o;
        return instance == null ? event.instance == null : instance.equals(event.instance);
    }

    public int hashCode()
    {
        return (instance != null ? instance.hashCode() : 0);
    }

    public abstract boolean isPost();
}
