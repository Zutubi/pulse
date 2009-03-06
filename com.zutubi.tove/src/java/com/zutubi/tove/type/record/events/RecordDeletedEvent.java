package com.zutubi.tove.type.record.events;

import com.zutubi.tove.type.record.RecordManager;

/**
 * Indicates a record has just been deleted.
 */
public class RecordDeletedEvent extends RecordEvent
{
    /**
     * Create a new record deleted event.
     *
     * @param source the source that is raising the event
     * @param path   path of the deleted record
     */
    public RecordDeletedEvent(RecordManager source, String path)
    {
        super(source, path);
    }

    @Override
    public String toString()
    {
        return "Record Deleted Event: " + path;
    }
}