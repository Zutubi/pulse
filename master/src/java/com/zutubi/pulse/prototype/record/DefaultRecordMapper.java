package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.core.PulseRuntimeException;
import com.zutubi.pulse.form.squeezer.SqueezeException;
import com.zutubi.pulse.form.squeezer.Squeezers;
import com.zutubi.pulse.form.squeezer.TypeSqueezer;
import com.zutubi.pulse.prototype.RecordPropertyInfo;
import com.zutubi.pulse.prototype.RecordTypeInfo;
import com.zutubi.pulse.prototype.RecordTypeRegistry;
import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Mapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 */
public class DefaultRecordMapper implements RecordMapper
{
    private static final Class[] BUILT_IN_TYPES = {Boolean.class, Boolean.TYPE, Byte.class, Byte.TYPE, Character.class, Character.TYPE, Double.class, Double.TYPE, Float.class, Float.TYPE, Integer.class, Integer.TYPE, Long.class, Long.TYPE, Short.class, Short.TYPE, String.class};
    private RecordTypeRegistry recordTypeRegistry;

    public Record toRecord(Object o)
    {
        Class<? extends Object> recordType = o.getClass();
        RecordTypeInfo info = recordTypeRegistry.getInfo(recordType);
        if (info == null)
        {
            throw new PulseRuntimeException("Class '" + recordType.getName() + "' not in record type registry");
        }

        SingleRecord record = new SingleRecord(info.getSymbolicName());
        for (RecordPropertyInfo property: info.getProperties())
        {
            try
            {
                Method method = property.getGetter();
                Object value = method.invoke(o);

                Object converted = convertValue(value, method.getGenericReturnType());
                if (converted != null)
                {
                    record.put(property.getName(), converted);
                }
            }
            catch (IllegalAccessException e)
            {
                throw new PulseRuntimeException("Unable to invoke getter for property'" + property.getName() + "' on record class '" + recordType.getName() + "': " + e.getMessage(), e);
            }
            catch (InvocationTargetException e)
            {
                throw new PulseRuntimeException("Execption thrown by getter for property'" + property.getName() + "' on record class '" + recordType.getName() + "': " + e.getMessage(), e);
            }
        }

        return record;
    }

    private Object convertValue(Object value, Type type)
    {
        if (value == null)
        {
            return Null.VALUE;
        }

        if (type instanceof Class)
        {
            Class clazz = (Class) type;

            if (isSimple(clazz))
            {
                // Simple properties (built in types, enums) are stored as
                // strings.
                return value.toString();
            }
            else
            {
                // It's an object, which should itself become a record.
                return toRecord(value);
            }
        }
        else if(type instanceof ParameterizedType)
        {
            // Already validated as a list or map.
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class clazz = (Class) parameterizedType.getRawType();
            if (List.class.isAssignableFrom(clazz))
            {
                return convertList((List) value, parameterizedType);
            }
            else if(Map.class.isAssignableFrom(clazz))
            {
                return convertMap((Map) value, parameterizedType);
            }
            else
            {
                assert(false);
            }
        }
        else
        {
            // Does not happen: already validated
            assert(false);
        }

        return null;
    }

    private List convertList(List list, ParameterizedType parameterizedType)
    {
        // A list of something, convert to a list of mapped converted
        // values
        final Type typeParameter = parameterizedType.getActualTypeArguments()[0];
        return CollectionUtils.map(list, new Mapping()
        {
            public Object map(Object o)
            {
                return convertValue(o, typeParameter);
            }
        });
    }

    private Map convertMap(Map map, ParameterizedType parameterizedType)
    {
        // Key type validated as String, but we need the value type.
        final Type valueType = parameterizedType.getActualTypeArguments()[1];
        return CollectionUtils.map(map, new Mapping()
        {
            public Object map(Object o)
            {
                return convertValue(o, valueType);
            }
        });
    }

    public static boolean isSimple(Class type)
    {
        return CollectionUtils.containsIdentity(BUILT_IN_TYPES, type) || type.isEnum();
    }

    public Object fromRecord(Record record)
    {
        RecordTypeInfo info = recordTypeRegistry.getInfo(record.getSymbolicName());
        if(info == null)
        {
            throw new PulseRuntimeException("Symbolic name '" + record.getSymbolicName() + "' not in record type registry");
        }

        Class type = info.getType();
        Object result;

        try
        {
            result = type.newInstance();
        }
        catch (Exception e)
        {
            throw new PulseRuntimeException("Unable to instantiate record type '" + type.getName() + "': " + e.getMessage(), e);
        }

        for(Map.Entry<String, Object> entry: record.entrySet())
        {
            String propertyName = entry.getKey();
            RecordPropertyInfo property = info.getProperty(propertyName);
            if(property == null)
            {
                throw new PulseRuntimeException("Unable to map key '" + propertyName + "' onto class '" + type.getName() + "': no such property");
            }
            
            Method setter = property.getSetter();
            Object value = unconvertValue(entry.getValue(), property.getType());

            try
            {
                setter.invoke(result, value);
            }
            catch (IllegalAccessException e)
            {
                throw new PulseRuntimeException("Unable to invoke setter for property'" + propertyName + "' on record class '" + type.getName() + "': " + e.getMessage(), e);
            }
            catch (InvocationTargetException e)
            {
                throw new PulseRuntimeException("Execption thrown by setter for property'" + propertyName + "' on record class '" + type.getName() + "': " + e.getMessage(), e);
            }
        }

        return result;
    }

    private Object unconvertValue(Object value, Type targetType)
    {
        if(value == Null.VALUE)
        {
            return null;
        }

        // The object better be one of Record, String, or List!
        if(value instanceof String)
        {
            return unconvertSimple((String) value, targetType);
        }
        else if(value instanceof Record)
        {
            if(!(targetType instanceof Class))
            {
                throw new PulseRuntimeException("Unexpected record for target type '" + targetType + "'");
            }

            Class clazz = (Class) targetType;
            Object result = fromRecord((Record) value);

            // Check it is the expected type
            if(!clazz.isAssignableFrom(result.getClass()))
            {
                throw new PulseRuntimeException("Object of unexpected type '" + result.getClass() + "' for target type '" + targetType + "'");
            }

            return result;
        }
        else if(value instanceof List)
        {
            return unconvertList((List) value, targetType);
        }
        else if(value instanceof Map)
        {
            return unconvertMap((Map) value, targetType);
        }
        else
        {
            throw new PulseRuntimeException("Unexpected type '" + value.getClass().getName() + "' in record graph");
        }
    }

    private Object unconvertSimple(String value, Type targetType)
    {
        if(!(targetType instanceof Class) || !isSimple((Class) targetType))
        {
            throw new PulseRuntimeException("Unable to unconvert value: target type '" + targetType + "' is not simple");
        }

        Class targetClass = (Class) targetType;
        if(targetClass.isEnum())
        {
            return Enum.valueOf(targetClass, value);
        }
        else
        {
            // TODO: squeezy on the other endy
            TypeSqueezer squeezer = Squeezers.findSqueezer((Class) targetType);
            try
            {
                return squeezer.unsqueeze(value);
            }
            catch (SqueezeException e)
            {
                throw new PulseRuntimeException(e);
            }
        }
    }

    private List unconvertList(List value, Type targetType)
    {
        ParameterizedType parameterizedType = (ParameterizedType) targetType;
        Class clazz = (Class) parameterizedType.getRawType();

        // We just need to validate that the setter is also expecting a list.
        // The specifics of the list are validated on registration.
        if(List.class.isAssignableFrom(clazz))
        {
            final Type[] typeArguments = parameterizedType.getActualTypeArguments();
            final Type typeParameter = typeArguments[0];

            // OK, checks out, map it
            return CollectionUtils.map(value, new Mapping()
            {
                public Object map(Object o)
                {
                    return unconvertValue(o, typeParameter);
                }
            });
        }
        else
        {
            throw new PulseRuntimeException("Unable to unconvert list: target type '" + targetType + "' is not a list");
        }
    }

    private Map unconvertMap(Map map, Type targetType)
    {
        ParameterizedType parameterizedType = (ParameterizedType) targetType;
        Class clazz = (Class) parameterizedType.getRawType();

        // We just need to validate that the setter is also expecting a map.
        // The specifics of the list are validated on registration.
        if(Map.class.isAssignableFrom(clazz))
        {
            final Type[] typeArguments = parameterizedType.getActualTypeArguments();
            final Type valueType = typeArguments[1];

            // OK, checks out, map it
            return CollectionUtils.map(map, new Mapping()
            {
                public Object map(Object o)
                {
                    return unconvertValue(o, valueType);
                }
            });
        }
        else
        {
            throw new PulseRuntimeException("Unable to unconvert map: target type '" + targetType + "' is not a map");
        }
    }

    public void setRecordTypeRegistry(RecordTypeRegistry recordTypeRegistry)
    {
        this.recordTypeRegistry = recordTypeRegistry;
    }
}
