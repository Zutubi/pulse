package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 * @see com.zutubi.prototype.config.ConfigurationListener#postSave(String, Object, String, Object)
 */
public class PostSaveEvent extends ConfigurationEvent
{
    private String path;
    private Object oldInstance;
    private String newPath;
    private Object newInstance;

    public PostSaveEvent(Object source, String path, Object oldInstance, String newPath, Object newInstance)
    {
        super(source, path);
        this.path = path;
        this.oldInstance = oldInstance;
        this.newPath = newPath;
        this.newInstance = newInstance;
    }

    public String getPath()
    {
        return path;
    }

    public Object getOldInstance()
    {
        return oldInstance;
    }

    public String getNewPath()
    {
        return newPath;
    }

    public Object getNewInstance()
    {
        return newInstance;
    }
}
