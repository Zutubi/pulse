package com.zutubi.pulse.prototype.record;

import java.lang.reflect.Method;

/**
 * Information about a property that holds a single subrecord.
 */
public class SubrecordRecordPropertyInfo extends AbstractRecordPropertyInfo
{
    private RecordTypeInfo subrecordType;

    public SubrecordRecordPropertyInfo(String name, Method getter, Method setter, RecordTypeInfo subrecordType)
    {
        super(name, getter, setter);
        this.subrecordType = subrecordType;
    }

    public RecordTypeInfo getSubrecordType()
    {
        return subrecordType;
    }

    public Object convertValue(Object value, RecordMapper mapper)
    {
        return mapper.toRecord(value);
    }

    public Object unconvertValue(Object value, RecordMapper mapper) throws RecordConversionException
    {
        if(!(value instanceof Record))
        {
            throw new RecordConversionException("Unexpected value type '" + value.getClass().getName() + "' for subrecord property '" + getName() + "'");
        }

        Record record = (Record) value;
        Object result = mapper.fromRecord(record);
        if(!(subrecordType.getType().isAssignableFrom(result.getClass())))
        {
            throw new RecordConversionException("Unexpected type '" + result.getClass().getName() + "' for subrecord property '" + getName() + "' (expecting instance of '" + subrecordType.getClass().getName() + "')");
        }
        
        return result;
    }
}
