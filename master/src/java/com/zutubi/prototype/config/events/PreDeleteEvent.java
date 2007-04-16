package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 * @see com.zutubi.prototype.config.ConfigurationListener#preDelete(String, Object)
 */
public class PreDeleteEvent extends ConfigurationEvent
{
    private String path;
    private Object instance;

    public PreDeleteEvent(Object source, String path, Object instance)
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
