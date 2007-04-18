package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 */
public class PreDeleteEvent extends ConfigurationEvent
{
    private Object instance;

    public PreDeleteEvent(ConfigurationPersistenceManager source, String path, Object instance)
    {
        super(source, path);
        this.instance = instance;
    }

    public Object getInstance()
    {
        return instance;
    }

    public String toString()
    {
        return "Pre Delete Event: " + getPath();
    }
}
