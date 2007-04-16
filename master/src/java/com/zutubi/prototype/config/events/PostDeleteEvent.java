package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 * @see com.zutubi.prototype.config.ConfigurationListener#postDelete(String, Object) 
 */
public class PostDeleteEvent extends ConfigurationEvent
{
    private String path;
    private Object oldInstance;

    public PostDeleteEvent(Object source, String path, Object oldInstance)
    {
        super(source, path);
        this.path = path;
        this.oldInstance = oldInstance;
    }

    public String getPath()
    {
        return path;
    }

    public Object getOldInstance()
    {
        return oldInstance;
    }
}
