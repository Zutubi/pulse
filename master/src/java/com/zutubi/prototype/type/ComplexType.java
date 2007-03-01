package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;

/**
 */
public interface ComplexType extends Type
{
    String getSymbolicName();

    String insert(String path, Record newRecord, RecordManager recordManager);

    Record createNewRecord();

    boolean isTemplated();
}
