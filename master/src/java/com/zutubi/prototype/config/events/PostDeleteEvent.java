package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 */
public class PostDeleteEvent extends ConfigurationEvent
{
    private Object oldInstance;

    public PostDeleteEvent(ConfigurationPersistenceManager source, String path, Object oldInstance)
    {
        super(source, path);
        this.oldInstance = oldInstance;
    }

    public Object getOldInstance()
    {
        return oldInstance;
    }

    public String toString()
    {
        return "Post Delete Event: " + getPath();
    }
}
