package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.prototype.Scope;

/**
 */
public class DefaultRecordManager implements RecordManager
{
    public Record load(Scope scope, String id)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void store(Record record)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public void delete(Record record)
    {
        throw new RuntimeException("Method not implemented.");
    }
}
