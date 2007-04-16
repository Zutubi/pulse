package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 */
public class PreSaveEvent extends ConfigurationEvent
{
    private String path;
    private Object oldInstance;

    public PreSaveEvent(ConfigurationPersistenceManager source, String path, Object oldInstance)
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
