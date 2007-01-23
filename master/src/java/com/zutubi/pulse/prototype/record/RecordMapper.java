package com.zutubi.pulse.prototype.record;

/**
 */
public interface RecordMapper
{
    Record toRecord(Object o);
    Object fromRecord(Record record);
}
