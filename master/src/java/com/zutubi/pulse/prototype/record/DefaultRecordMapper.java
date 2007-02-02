package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.core.PulseRuntimeException;
import com.zutubi.pulse.form.squeezer.SqueezeException;
import com.zutubi.pulse.form.squeezer.Squeezers;
import com.zutubi.pulse.form.squeezer.TypeSqueezer;
import com.zutubi.pulse.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

                Object converted = convertValue(value, property);
                if (converted != null)
                {
                    record.put(property.getName(), converted);
                }
            }
            catch (IllegalAccessException e)
            {
                throw new PulseRuntimeException("Unable to invoke getter for property'" + property.getName() + "' on record class '" + info.getType().getName() + "': " + e.getMessage(), e);
            }
            catch (InvocationTargetException e)
            {
                throw new PulseRuntimeException("Execption thrown by getter for property'" + property.getName() + "' on record class '" + info.getType().getName() + "': " + e.getMessage(), e);
            }
        }

        return record;
    }

    private Object convertValue(Object value, RecordPropertyInfo propertyInfo)
    {
        if (value == null)
        {
            return Null.VALUE;
        }

        return propertyInfo.convertValue(value, this);
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
            Object value = unconvertValue(entry.getValue(), property);

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

    public Object unconvertSimpleValue(String stringValue, Class targetClass) throws RecordConversionException
    {
        if(targetClass.isEnum())
        {
            return Enum.valueOf(targetClass, stringValue);
        }
        else
        {
            // TODO: squeezy on the other endy
            TypeSqueezer squeezer = Squeezers.findSqueezer(targetClass);
            try
            {
                return squeezer.unsqueeze(stringValue);
            }
            catch (SqueezeException e)
            {
                // TODO: ensure we get context
                throw new RecordConversionException(e);
            }
        }
    }

    private Object unconvertValue(Object value, RecordPropertyInfo propertyInfo)
    {
        if(value == Null.VALUE)
        {
            return null;
        }

        return propertyInfo.unconvertValue(value, this);
    }

    public void setRecordTypeRegistry(RecordTypeRegistry recordTypeRegistry)
    {
        this.recordTypeRegistry = recordTypeRegistry;
    }
}
