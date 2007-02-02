package com.zutubi.pulse.prototype.record;

import java.lang.reflect.Method;

/**
 * Information for a simple property, which is used to store a primitive type
 * or enumeration.
 */
public class SimpleRecordPropertyInfo extends AbstractRecordPropertyInfo
{
    public SimpleRecordPropertyInfo(String name, Method getter, Method setter)
    {
        super(name, getter, setter);
    }

    public Class getValueClass()
    {
        return (Class)getType();
    }

    public Object convertValue(Object value, RecordMapper mapper)
    {
        return value.toString();
    }

    public Object unconvertValue(Object value, RecordMapper mapper) throws RecordConversionException
    {
        if(!(value instanceof String))
        {
            throw new RecordConversionException("Unexpected value type '" + value.getClass().getName() + "' for simple property '" + getName() + "'");
        }

        return mapper.unconvertSimpleValue((String) value, getValueClass());
    }
}
