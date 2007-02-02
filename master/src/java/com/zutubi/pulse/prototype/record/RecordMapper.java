package com.zutubi.pulse.prototype.record;

/**
 */
public interface RecordMapper
{
    Record toRecord(Object o);

    Object fromRecord(Record record);

    Object unconvertSimpleValue(String stringValue, Class targetClass) throws RecordConversionException;
}
