package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.Record;

/**
 */
public interface ComplexType extends Type
{
    String getSymbolicName();

    String getSavePath(Record collection, Record record);

    String getInsertionPath(Record collection, Record record);

    Record createNewRecord();

    boolean isTemplated();
}
