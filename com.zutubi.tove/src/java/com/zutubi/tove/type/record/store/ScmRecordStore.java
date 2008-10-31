package com.zutubi.tove.type.record.store;

import com.zutubi.tove.type.record.Record;

/**
 *
 *
 */
public class ScmRecordStore implements RecordStore
{
    public void insert(String path, Record record)
    {
    }

    public void update(String path, Record record)
    {
    }

    public Record delete(String path)
    {
        return null;
    }

    public Record select()
    {
        return null;
    }

    public Record exportRecords()
    {
        return null;
    }

    public void importRecords(Record r)
    {

    }

    public boolean prepare()
    {
        return false;
    }

    public void commit()
    {

    }

    public void rollback()
    {

    }
}
