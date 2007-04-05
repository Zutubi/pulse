package com.zutubi.prototype.type.record;

/**
 */
public class MockRecordSerialiser implements RecordSerialiser
{
    public void serialise(String path, MutableRecord record, boolean deep) throws RecordSerialiseException
    {
    }

    public MutableRecord deserialise(String path) throws RecordSerialiseException
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public void delete(String path) throws RecordSerialiseException
    {
    }
}
