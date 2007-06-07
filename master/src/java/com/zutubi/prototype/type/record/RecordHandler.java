package com.zutubi.prototype.type.record;

/**
 * Callback interface for handling records as they are traversed.
 */
public interface RecordHandler
{
    void handle(String path, Record record);
}
