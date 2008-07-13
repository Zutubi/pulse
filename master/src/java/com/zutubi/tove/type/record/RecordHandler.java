package com.zutubi.tove.type.record;

/**
 * Callback interface for handling records as they are traversed.
 */
public interface RecordHandler
{
    void handle(String path, Record record);
}
