package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 */
public class PreInsertEvent extends ConfigurationEvent
{
    private String path;

    public PreInsertEvent(ConfigurationPersistenceManager source, String path)
    {
        super(source, path);
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }
}
