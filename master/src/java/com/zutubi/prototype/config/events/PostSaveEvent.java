package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationTemplateManager;

/**
 */
public class PostSaveEvent extends ConfigurationEvent
{
    private Object oldInstance;
    private String newPath;
    private Object newInstance;

    public PostSaveEvent(ConfigurationTemplateManager source, String path, Object oldInstance, String newPath, Object newInstance)
    {
        super(source, path);
        this.oldInstance = oldInstance;
        this.newPath = newPath;
        this.newInstance = newInstance;
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

    public String toString()
    {
        return "Post Save Event: " + getPath();
    }
}
