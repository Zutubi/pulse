package com.zutubi.tove.type.record.events;

import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

/**
 * Indicates a record has just been updated.
 */
public class RecordUpdatedEvent extends RecordEvent
{
    private Record newRecord;

    /**
     * Create a new record updated event.
     *
     * @param source    the source that is raising the event
     * @param path      path of the updated record
     * @param newRecord the new record values
     */
    public RecordUpdatedEvent(RecordManager source, String path, Record newRecord)
    {
        super(source, path);
        this.newRecord = newRecord;
    }

    /**
     * Returns the new record values.
     *
     * @return the updated record values
     */
    public Record getNewRecord()
    {
        return newRecord;
    }

    @Override
    public String toString()
    {
        return "Record Updated Event: " + path;
    }
}