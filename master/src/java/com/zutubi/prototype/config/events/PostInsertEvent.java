package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 */
public class PostInsertEvent extends ConfigurationEvent
{
    private String insertedPath;
    private Object newInstance;

    public PostInsertEvent(ConfigurationPersistenceManager source, String path, String insertedPath, Object newInstance)
    {
        super(source, path);
        this.newInstance = newInstance;
        this.insertedPath = insertedPath;
    }

    public String getInsertedPath()
    {
        return insertedPath;
    }

    public Object getNewInstance()
    {
        return newInstance;
    }

    public String toString()
    {
        return "Post Insert Event: " + getPath();
    }
}
