package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;

/**
 * @see com.zutubi.prototype.config.ConfigurationListener#preInsert(String) 
 */
public class PreInsertEvent extends ConfigurationEvent
{
    private String path;

    public PreInsertEvent(Object source, String path)
    {
        super(source, path);
        this.path = path;
    }

    public String getPath()
    {
        return path;
    }
}
