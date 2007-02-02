package com.zutubi.pulse.prototype.record;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds information about a type in the record type registry, including the
 * property getter and setter methods.
 */
public class RecordTypeInfo
{
    private String symbolicName;
    private Class type;
    private Map<String, RecordPropertyInfo> properties = new HashMap<String, RecordPropertyInfo>();

    public RecordTypeInfo(String symbolicName, Class type) throws InvalidRecordTypeException
    {
        this.symbolicName = symbolicName;
        this.type = type;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public Class getType()
    {
        return type;
    }

    public RecordPropertyInfo getProperty(String name)
    {
        return properties.get(name);
    }

    void addProperty(RecordPropertyInfo propertyInfo)
    {
        properties.put(propertyInfo.getName(), propertyInfo);
    }

    public Iterable<? extends RecordPropertyInfo> getProperties()
    {
        return properties.values();
    }
}
