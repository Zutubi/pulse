package com.zutubi.prototype.config.events;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.record.MutableRecord;

/**
 */
public class PreInsertEvent extends ConfigurationEvent
{
    private MutableRecord record;

    public PreInsertEvent(ConfigurationPersistenceManager source, String path, MutableRecord record)
    {
        super(source, path);
        this.record = record;
    }

    public MutableRecord getRecord()
    {
        return record;
    }

    public String toString()
    {
        return "Pre Insert Event: " + getPath();
    }
}
