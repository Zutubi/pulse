package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 * @see com.zutubi.prototype.config.ConfigurationListener#postInsert(String, String, Object)  
 */
public class PostInsertEvent extends ConfigurationEvent
{
    private String path;
    private String insertedPath;
    private Object newInstance;

    public PostInsertEvent(Object source, String path, String insertedPath, Object newInstance)
    {
        super(source, path);
        this.path = path;
        this.newInstance = newInstance;
        this.insertedPath = insertedPath;
    }

    public String getPath()
    {
        return path;
    }

    public String getInsertedPath()
    {
        return insertedPath;
    }

    public Object getNewInstance()
    {
        return newInstance;
    }
}
