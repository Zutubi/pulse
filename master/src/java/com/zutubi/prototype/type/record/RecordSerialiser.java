package com.zutubi.prototype.type.record;

/**
 * An interface for storing and loading records from permanent storage.
 */
public interface RecordSerialiser
{
    void serialise(String path, MutableRecord record, boolean deep) throws RecordSerialiseException;

    MutableRecord deserialise(String path) throws RecordSerialiseException;

    void delete(String path) throws RecordSerialiseException;
}
