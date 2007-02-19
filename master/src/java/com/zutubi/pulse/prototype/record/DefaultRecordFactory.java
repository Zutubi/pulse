package com.zutubi.pulse.prototype.record;

/**
 * Basic record factory, which creates the simplest records.
 * @deprecated
 */
public class DefaultRecordFactory implements RecordFactory
{
    public Record create(String symbolicName)
    {
        return new SingleRecord(symbolicName);
    }
}
