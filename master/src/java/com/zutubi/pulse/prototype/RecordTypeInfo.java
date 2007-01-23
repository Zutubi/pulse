package com.zutubi.pulse.prototype;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
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
        BeanInfo info;
        
        try
        {
            info = Introspector.getBeanInfo(type, Object.class);
            for(PropertyDescriptor descriptor: info.getPropertyDescriptors())
            {
                RecordPropertyInfo property = new RecordPropertyInfo(descriptor.getName(), descriptor.getReadMethod(), descriptor.getWriteMethod());
                validatePropertyType(property.getName(), property.getType());
                properties.put(property.getName(), property);
            }
        }
        catch (IntrospectionException e)
        {
            throw new InvalidRecordTypeException("Unable to introspect on record type '" + type.getName() + "': " + e.getMessage(), e);
        }
    }

    private void validatePropertyType(String propertyName, Type type) throws InvalidRecordTypeException
    {
        if(type instanceof Class)
        {
            // We support any classes, but they must be registered.
            // Unfortunately, there is no way to know if they will be
            // registered in the future, so we can't verify this now.
            return;
        }
        else if(type instanceof ParameterizedType)
        {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if(rawType instanceof Class)
            {
                Class clazz = (Class) rawType;

                if (List.class.isAssignableFrom(clazz))
                {
                    validateListType(propertyName, parameterizedType);
                }
                else if(Map.class.isAssignableFrom(clazz))
                {
                    validateMapType(propertyName, parameterizedType);
                }
                else
                {
                    // We don't support arbitrary generic types
                    throw new InvalidRecordTypeException("Property '" + propertyName + "' has unsupported generic type '" + type + "': only lists and maps are supported");
                }
            }
            else
            {
                throw new InvalidRecordTypeException("Property '" + propertyName + "' has unsupported parameterised type '" + type + "' (raw type is not a class)");
            }
        }
        else
        {
            throw new InvalidRecordTypeException("Property '" + propertyName + "' has unsupported type '" + type + "'");
        }
    }

    private void validateListType(String propertyName, ParameterizedType type) throws InvalidRecordTypeException
    {
        if(!(type.getActualTypeArguments()[0] instanceof Class))
        {
            // We aren't smart enough to handle nested generics...
            throw new InvalidRecordTypeException("Property '" + propertyName + "' has unsupported nested generic type '" + type + "' (list items must have a class type, not a generic type)");
        }
    }

    private void validateMapType(String propertyName, ParameterizedType type) throws InvalidRecordTypeException
    {
        Type[] typeArguments = type.getActualTypeArguments();
        Type keyType = typeArguments[0];
        Type valueType = typeArguments[1];

        if(keyType != String.class)
        {
            throw new InvalidRecordTypeException("Property '" + propertyName + "' has unsupported map type '" + type + "' (map keys must be strings)");
        }

        if(!(valueType instanceof Class))
        {
            // We aren't smart enough to handle nested generics...
            throw new InvalidRecordTypeException("Property '" + propertyName + "' has unsupported nested generic type '" + type + "' (map values must have a class type, not a generic type)");            
        }
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

    public Iterable<? extends RecordPropertyInfo> getProperties()
    {
        return properties.values();
    }
}
