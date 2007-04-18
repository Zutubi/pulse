package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 */
public class PreInsertEvent extends ConfigurationEvent
{
    public PreInsertEvent(ConfigurationPersistenceManager source, String path)
    {
        super(source, path);
    }

    public String toString()
    {
        return "Pre Insert Event: " + getPath();
    }
}
