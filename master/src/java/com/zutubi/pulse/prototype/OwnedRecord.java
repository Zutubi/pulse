package com.zutubi.pulse.prototype;

import com.zutubi.pulse.prototype.record.Record;

/**
 * <class comment/>
 */
public class OwnedRecord
{
    Record record;
    String owner;

    public OwnedRecord(Record record, String owner)
    {
        this.record = record;
        this.owner = owner;
    }

    public Record getRecord()
    {
        return record;
    }

    public String getOwner()
    {
        return owner;
    }
}
