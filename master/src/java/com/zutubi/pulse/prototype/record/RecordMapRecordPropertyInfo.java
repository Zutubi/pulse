package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Mapping;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Information for a property that stores a map of records.
 * @deprecated
 */
public class RecordMapRecordPropertyInfo extends AbstractRecordPropertyInfo
{
    private RecordTypeInfo recordType;

    public RecordMapRecordPropertyInfo(String name, Method getter, Method setter, RecordTypeInfo recordType)
    {
        super(name, getter, setter);
        this.recordType = recordType;
    }

    public RecordTypeInfo getRecordType()
    {
        return recordType;
    }

    public Object convertValue(Object value, final RecordMapper mapper)
    {
        return CollectionUtils.map((Map)value, new Mapping()
        {
            public Object map(Object o)
            {
                return mapper.toRecord(o);
            }
        });

    }

    public Object unconvertValue(Object value, final RecordMapper mapper) throws RecordConversionException
    {
        if (!(value instanceof Map))
        {
            throw new RecordConversionException("Unexpected value type '" + value.getClass().getName() + "' for record map property '" + getName() + "'");
        }

        return CollectionUtils.map((Map)value, new Mapping()
        {
            public Object map(Object o)
            {
                if(!(o instanceof Record))
                {
                    throw new RecordConversionException("Unexpected entry value type '" + o.getClass().getName() + "' in record map property '" + getName() + "'");
                }

                Record record = (Record) o;
                Object result = mapper.fromRecord(record);
                if(!recordType.getType().isAssignableFrom(result.getClass()))
                {
                    throw new RecordConversionException("Unexpected type '" + result.getClass() + "' for entry in record map property '" + getName() + "' (expecting instance of '" + recordType.getClass().getName() + "')");
                }
                return result;
            }
        });
    }
}
