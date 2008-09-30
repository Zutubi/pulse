package com.zutubi.tove.type.record;

/**
 * An interface for storing and loading records from permanent storage.
 *
 */
public interface RecordSerialiser
{
    void serialise(String path, Record record, boolean deep) throws RecordSerialiseException;

    MutableRecord deserialise(String path, RecordHandler handler) throws RecordSerialiseException;
}
