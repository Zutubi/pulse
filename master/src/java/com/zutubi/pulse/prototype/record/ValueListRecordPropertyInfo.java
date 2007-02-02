package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Mapping;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Information for a property that stores a list of primitive values.
 */
public class ValueListRecordPropertyInfo extends AbstractRecordPropertyInfo
{
    private Class valueType;

    public ValueListRecordPropertyInfo(String name, Method getter, Method setter, Class valueType)
    {
        super(name, getter, setter);
        this.valueType = valueType;
    }

    public Class getValueType()
    {
        return valueType;
    }

    public Object convertValue(Object value, RecordMapper mapper)
    {
        return CollectionUtils.map((List) value, new Mapping()
        {
            public Object map(Object o)
            {
                return o.toString();
            }
        });
    }

    public Object unconvertValue(Object value, final RecordMapper mapper) throws RecordConversionException
    {
        if (!(value instanceof List))
        {
            throw new RecordConversionException("Unexpected value type '" + value.getClass().getName() + "' for value list property '" + getName() + "'");
        }

        return CollectionUtils.map((List) value, new Mapping()
        {
            public Object map(Object o)
            {
                if(!(o instanceof String))
                {
                    throw new RecordConversionException("Unexpected entry type '" + o.getClass().getName() + "' in value list property '" + getName() + "'");
                }
                
                return mapper.unconvertSimpleValue((String) o, valueType);
            }
        });
    }
}
