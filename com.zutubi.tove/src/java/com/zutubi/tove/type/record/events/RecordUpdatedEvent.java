package com.zutubi.tove.type.record.events;

import com.zutubi.tove.type.record.RecordManager;

/**
 * Indicates a record has just been updated.
 */
public class RecordUpdatedEvent extends RecordEvent
{
    /**
     * Create a new record updated event.
     *
     * @param source the source that is raising the event
     * @param path   path of the updated record
     */
    public RecordUpdatedEvent(RecordManager source, String path)
    {
        super(source, path);
    }

    @Override
    public String toString()
    {
        return "Record Updated Event: " + path;
    }
}