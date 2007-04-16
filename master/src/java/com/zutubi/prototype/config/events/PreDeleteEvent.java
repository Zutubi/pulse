package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 */
public class PreDeleteEvent extends ConfigurationEvent
{
    private String path;
    private Object instance;

    public PreDeleteEvent(ConfigurationPersistenceManager source, String path, Object instance)
    {
        super(source, path);
        this.path = path;
        this.instance = instance;
    }

    public String getPath()
    {
        return path;
    }

    public Object getInstance()
    {
        return instance;
    }
}
