package com.zutubi.tove.type.record.events;

import com.zutubi.tove.type.record.RecordManager;

/**
 * Indicates a record has just been inserted.
 */
public class RecordInsertedEvent extends RecordEvent
{
    /**
     * Create a new record inserted event.
     *
     * @param source the source that is raising the event
     * @param path   path of the inserted record
     */
    public RecordInsertedEvent(RecordManager source, String path)
    {
        super(source, path);
    }

    @Override
    public String toString()
    {
        return "Record Inserted Event: " + path;
    }
}
