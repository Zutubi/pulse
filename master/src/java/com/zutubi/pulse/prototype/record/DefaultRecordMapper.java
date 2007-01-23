package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.core.PulseRuntimeException;
import com.zutubi.pulse.prototype.RecordTypeRegistry;
import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Mapping;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 */
public class DefaultRecordMapper implements RecordMapper
{
    private static final Class[] BUILT_IN_TYPES = {Boolean.class, Boolean.TYPE, Byte.class, Byte.TYPE, Character.class, Character.TYPE, Double.class, Double.TYPE, Float.class, Float.TYPE, Integer.class, Integer.TYPE, Long.class, Long.TYPE, Short.class, Short.TYPE, String.class};
    private RecordTypeRegistry recordTypeRegistry;

    public Record toRecord(Object o)
    {
        Class<? extends Object> recordType = o.getClass();
        String symbolicName = recordTypeRegistry.getSymbolicName(recordType);
        if (symbolicName == null)
        {
            throw new PulseRuntimeException("Class '" + recordType.getName() + "' not in record type registry");
        }

        SingleRecord record = new SingleRecord(symbolicName);
        BeanInfo beanInfo = null;
        try
        {
            beanInfo = Introspector.getBeanInfo(recordType, Object.class);
        }
        catch (IntrospectionException e)
        {
            throw new PulseRuntimeException("Unable to introspect on record class '" + recordType.getName() + "'", e);
        }

        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors())
        {
            try
            {
                Method method = descriptor.getReadMethod();
                Object value = method.invoke(o);

                Object converted = convertValue(value, method.getGenericReturnType());
                if (converted != null)
                {
                    record.put(descriptor.getName(), converted);
                }
            }
            catch (IllegalAccessException e)
            {
                throw new PulseRuntimeException("Unable to invoke getter for property'" + descriptor.getName() + "' on record class '" + recordType.getName() + "': " + e.getMessage(), e);
            }
            catch (InvocationTargetException e)
            {
                throw new PulseRuntimeException("Execption thrown by getter for property'" + descriptor.getName() + "' on record class '" + recordType.getName() + "': " + e.getMessage(), e);
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
            // We support collections if parameterised
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if(rawType instanceof Class)
            {
                Class clazz = (Class) rawType;

                if (List.class.isAssignableFrom(clazz))
                {
                    return convertList(value, parameterizedType, type);
                }
                else
                {
                    // We don't support arbitrary generic types
                    throw new PulseRuntimeException("Unsupported generic type '" + type + "': only lists and maps are supported");
                }
            }
            else
            {
                throw new PulseRuntimeException("Unsupported parameterised type '" + type + "'");
            }
        }

        return null;
    }

    private Object convertList(Object value, ParameterizedType parameterizedType, Type type)
    {
        // A list of something, convert to a list of mapped converted
        // values
        List list = (List) value;
        final Type[] typeArguments = parameterizedType.getActualTypeArguments();
        final Type typeParameter = typeArguments[0];
        if(!(typeParameter instanceof Class))
        {
            // We aren't smart enough to handle nested generics...
            throw new PulseRuntimeException("Unsupported nested generic type '" + type + "'");
        }

        return CollectionUtils.map(list, new Mapping()
        {
            public Object map(Object o)
            {
                return convertValue(o, typeParameter);
            }
        });
    }

    private boolean isSimple(Class type)
    {
        return CollectionUtils.containsIdentity(BUILT_IN_TYPES, type) || type.isEnum();
    }

    public Object fromRecord(Record record)
    {
        Class type = recordTypeRegistry.getType(record.getSymbolicName());
        if(type == null)
        {
            throw new PulseRuntimeException("Unable to convert record: Unrecognised symbolic name '" + record.getSymbolicName() + "'");
        }

        Object result = null;

        try
        {
            result = type.newInstance();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

//        BeanInfo beanInfo = Introspector.getBeanInfo(type, Object.class);
        return result;
    }

    public void setRecordTypeRegistry(RecordTypeRegistry recordTypeRegistry)
    {
        this.recordTypeRegistry = recordTypeRegistry;
    }
}

