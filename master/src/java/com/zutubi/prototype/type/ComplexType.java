package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.Record;

/**
 */
public interface ComplexType extends Type
{
    String getSymbolicName();

    String getSavePath(String path, Record record);

    String getInsertionPath(String path, Record record);

    MutableRecord createNewRecord(boolean applyDefaults);

    boolean isTemplated();

    Type getActualPropertyType(String propertyName, Object propertyValue);

}
