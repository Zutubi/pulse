package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 */
public class PreSaveEvent extends ConfigurationEvent
{
    private Object oldInstance;

    public PreSaveEvent(ConfigurationPersistenceManager source, String path, Object oldInstance)
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
        return "Pre Save Event: " + getPath();
    }
}
