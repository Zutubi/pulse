package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.core.PulseRuntimeException;
import com.zutubi.pulse.util.AnnotationUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A registry that maps from record symbolic names to classes and vice-versa.
 * It is used to store the type information with a record without tying it to
 * a specific class - the symbolic name adds a level of indirection so that
 * the class can change over time.
 */
public class RecordTypeRegistry
{
    private Map<String, RecordTypeInfo> nameToInfo = new HashMap<String, RecordTypeInfo>();
    private Map<Class, RecordTypeInfo> typeToInfo = new HashMap<Class, RecordTypeInfo>();

    public RecordTypeInfo register(String symbolicName, Class type) throws InvalidRecordTypeException
    {
        RecordTypeInfo info = new RecordTypeInfo(symbolicName, type);

        validateRecordType(type);
        BeanInfo beanInfo;

        try
        {
            beanInfo = Introspector.getBeanInfo(type, Object.class);
            for(PropertyDescriptor descriptor: beanInfo.getPropertyDescriptors())
            {
                validateAndAddProperty(info, descriptor);
            }
        }
        catch (IntrospectionException e)
        {
            throw new InvalidRecordTypeException("Unable to introspect on record type '" + type.getName() + "': " + e.getMessage(), e);
        }

        nameToInfo.put(symbolicName, info);
        typeToInfo.put(type, info);
        return info;
    }

    private void validateRecordType(Class type) throws InvalidRecordTypeException
    {
        try
        {
            type.getConstructor();
        }
        catch (NoSuchMethodException e)
        {
            throw new InvalidRecordTypeException("Record types must have a public, zero-argument constructor");
        }
    }

    private void validateAndAddProperty(RecordTypeInfo info, PropertyDescriptor descriptor) throws InvalidRecordTypeException, IntrospectionException
    {
        String propertyName = descriptor.getName();
        Method readMethod = descriptor.getReadMethod();
        Method writeMethod = descriptor.getWriteMethod();
        Type type = readMethod.getGenericReturnType();
        AbstractRecordPropertyInfo property;

        if(type instanceof Class)
        {
            Class clazz = (Class) type;
            if(DefaultRecordMapper.isSimple(clazz))
            {
                property = new SimpleRecordPropertyInfo(propertyName, readMethod, writeMethod);
            }
            else
            {
                property = new SubrecordRecordPropertyInfo(propertyName, readMethod, writeMethod, getRecordType(clazz));
            }
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
                    Class valueClass = validateListType(info, propertyName, parameterizedType);
                    property = new ValueListRecordPropertyInfo(propertyName, readMethod, writeMethod, valueClass);
                }
                else if(Map.class.isAssignableFrom(clazz))
                {
                    RecordTypeInfo valueType = validateMapType(info, propertyName, parameterizedType);
                    property = new RecordMapRecordPropertyInfo(propertyName, readMethod, writeMethod, valueType);
                }
                else
                {
                    // We don't support arbitrary generic types
                    throw getPropertyException(info, propertyName, "has unsupported generic type '" + type + "': only lists and maps are supported");
                }
            }
            else
            {
                throw getPropertyException(info, propertyName, "has unsupported parameterised type '" + type + "' (raw type is not a class)");
            }
        }
        else
        {
            throw getPropertyException(info, propertyName, "has unsupported type '" + type + "'");
        }

        property.addAnnotations(AnnotationUtils.annotationsFromProperty(descriptor));
        info.addProperty(property);
    }

    private Class validateListType(RecordTypeInfo info, String propertyName, ParameterizedType type) throws InvalidRecordTypeException
    {
        if(!(type.getActualTypeArguments()[0] instanceof Class))
        {
            // We aren't smart enough to handle nested generics...
            throw getPropertyException(info, propertyName, "has unsupported nested generic type '" + type + "' (list items must have a class type, not a generic type)");
        }

        Class clazz = (Class) type.getActualTypeArguments()[0];
        if(!DefaultRecordMapper.isSimple(clazz))
        {
            throw getPropertyException(info, propertyName, "has unsupported list type '" + type + "' (list items must be simple values)");
        }

        return clazz;
    }

    private RecordTypeInfo validateMapType(RecordTypeInfo info, String propertyName, ParameterizedType type) throws InvalidRecordTypeException
    {
        Type[] typeArguments = type.getActualTypeArguments();
        Type keyType = typeArguments[0];
        Type valueType = typeArguments[1];

        if(keyType != String.class)
        {
            throw getPropertyException(info, propertyName, "has unsupported map type '" + type + "' (map keys must be strings)");
        }

        if(!(valueType instanceof Class))
        {
            // We aren't smart enough to handle nested generics...
            throw getPropertyException(info, propertyName, "has unsupported nested generic type '" + type + "' (map values must have a class type, not a generic type)");
        }

        Class valueClass = (Class) valueType;
        if(DefaultRecordMapper.isSimple(valueClass))
        {
            throw getPropertyException(info, propertyName, "has unsupported map type '" + type + "' (map values must be record types)");
        }

        return getRecordType(valueClass);
    }

    private RecordTypeInfo getRecordType(Class clazz) throws InvalidRecordTypeException
    {
        RecordTypeInfo childType = typeToInfo.get(clazz);
        if(childType == null)
        {
            childType = register(clazz);
        }
        return childType;
    }

    private InvalidRecordTypeException getPropertyException(RecordTypeInfo info, String propertyName, String message)
    {
        return new InvalidRecordTypeException("Registering record class '" + info.getClass().getName() + "': Property '" + propertyName + "' " + message);
    }

    public RecordTypeInfo register(Class type) throws InvalidRecordTypeException
    {
        // TODO: why does Intellij insist on this cast?
        SymbolicName a = (SymbolicName) type.getAnnotation(SymbolicName.class);
        if(a == null)
        {
            throw new PulseRuntimeException("Unable to register class '" + type + "': no SymbolicName annotation");
        }

        return register(a.value(), type);
    }

    public Class getType(String symbolicName)
    {
        return nameToInfo.get(symbolicName).getType();
    }

    public RecordTypeInfo getInfo(String symbolicName)
    {
        return nameToInfo.get(symbolicName);
    }

    public String getSymbolicName(Class type)
    {
        return typeToInfo.get(type).getSymbolicName();
    }

    public RecordTypeInfo getInfo(Class type)
    {
        return typeToInfo.get(type);
    }
}
