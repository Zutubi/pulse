package com.zutubi.prototype.type.record;

/**
 */
public class MockRecordSerialiser implements RecordSerialiser
{
    public void serialise(String path, Record record, boolean deep) throws RecordSerialiseException
    {
    }

    public MutableRecord deserialise(String path, RecordHandler handler) throws RecordSerialiseException
    {
        return new MutableRecordImpl();
    }

    public void delete(String path) throws RecordSerialiseException
    {
    }
}
