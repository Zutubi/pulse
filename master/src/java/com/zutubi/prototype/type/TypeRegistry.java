package com.zutubi.prototype.type;

import com.zutubi.pulse.core.PulseRuntimeException;
import com.zutubi.pulse.prototype.record.SymbolicName;
import com.zutubi.pulse.util.AnnotationUtils;
import com.zutubi.pulse.util.CollectionUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class TypeRegistry
{
    private static final Class[] BUILT_IN_TYPES = {Boolean.class, Boolean.TYPE, Byte.class, Byte.TYPE, Character.class, Character.TYPE, Double.class, Double.TYPE, Float.class, Float.TYPE, Integer.class, Integer.TYPE, Long.class, Long.TYPE, Short.class, Short.TYPE, String.class};

    private Map<String, CompositeType> symbolicNameMapping = new HashMap<String, CompositeType>();
    
    private Map<Class, CompositeType> classMapping = new HashMap<Class, CompositeType>();

    private Map<Class, CompositeType> anonymousMapping = new HashMap<Class, CompositeType>();

    public CompositeType register(Class type) throws TypeException
    {
        // extract the symbolicName from an annotation.
        SymbolicName a = (SymbolicName) type.getAnnotation(SymbolicName.class);
        if(a == null)
        {
            throw new PulseRuntimeException("Unable to register class '" + type + "': no SymbolicName annotation");
        }

        // register
        return register(a.value(), type);
    }

    public CompositeType register(String symbolicName, Class clazz) throws TypeException
    {
        if (isSimple(clazz))
        {
            throw new IllegalArgumentException("Can not register simple types.");
        }

        if (getType(symbolicName) != null)
        {
            throw new IllegalArgumentException("Symbolic name '"+symbolicName+"' already in use");
        }

        // convert class into Type, and store.
        CompositeType type = new CompositeType(clazz, symbolicName);

        symbolicNameMapping.put(symbolicName, type);
        classMapping.put(clazz, type);

        buildType(type, true);

        return type;
    }

    private CompositeType buildType(CompositeType protoType, boolean recurse) throws TypeException
    {
        try
        {
            BeanInfo beanInfo = Introspector.getBeanInfo(protoType.getClazz(), Object.class);
            for(PropertyDescriptor descriptor: beanInfo.getPropertyDescriptors())
            {
                String propertyName = descriptor.getName();

                Method readMethod = descriptor.getReadMethod();
                Method writeMethod = descriptor.getWriteMethod();
                java.lang.reflect.Type type = readMethod.getGenericReturnType();

                if(type instanceof Class)
                {
                    Class clazz = (Class) type;
                    if(isSimple(clazz))
                    {
                        protoType.addProperty(propertyName, new PrimitiveType(clazz), writeMethod, readMethod);
                    }
                    else
                    {
                        if (recurse)
                        {
                            Type subType = getType(clazz);
                            if (subType == null)
                            {
                                subType = register(clazz);
                            }
                            protoType.addProperty(propertyName, subType, writeMethod, readMethod);
                        }
                    }
                }
                else if(type instanceof ParameterizedType)
                {
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    java.lang.reflect.Type rawType = parameterizedType.getRawType();
                    if(rawType instanceof Class)
                    {
                        Class clazz = (Class) rawType;

                        if (List.class.isAssignableFrom(clazz))
                        {
                            Class valueClass = (Class) parameterizedType.getActualTypeArguments()[0];
                            Type collectionType;
                            if(isSimple(valueClass))
                            {
                                collectionType = new PrimitiveType(valueClass);
                            }
                            else
                            {
                                collectionType = getType(valueClass);
                                if (collectionType == null && recurse)
                                {
                                    collectionType = register(valueClass.getName(), valueClass);
                                }
                            }

                            ListType listType = new ListType(clazz);
                            listType.setCollectionType(collectionType);

                            protoType.addProperty(propertyName, listType, writeMethod, readMethod);
                        }
                        else if(Map.class.isAssignableFrom(clazz))
                        {
                            java.lang.reflect.Type[] typeArguments = parameterizedType.getActualTypeArguments();
                            java.lang.reflect.Type valueType = typeArguments[1];

                            Class valueClass = (Class) valueType;
                            Type collectionType;
                            if(isSimple(valueClass))
                            {
                                collectionType = new PrimitiveType(valueClass);
                            }
                            else
                            {
                                collectionType = getType(valueClass);
                                if (collectionType == null && recurse)
                                {
                                    collectionType = register(valueClass.getName(), valueClass);
                                }
                            }

                            MapType mapType = new MapType((Class) valueType);
                            mapType.setCollectionType(collectionType);
                            
                            protoType.addProperty(propertyName, mapType, writeMethod, readMethod);
                        }
                    }
                }
                AnnotationUtils.annotationsFromProperty(descriptor);
            }
            return protoType;
        }
        catch (IntrospectionException e)
        {
            throw new TypeException(e);
        }
    }

    public CompositeType registerAnonymous(Class clazz) throws TypeException
    {
        if (!anonymousMapping.containsKey(clazz))
        {
            CompositeType type = new CompositeType(clazz);
            anonymousMapping.put(clazz, type);
            buildType(type, false);
        }
        return anonymousMapping.get(clazz);
    }

    public CompositeType getType(String symbolicName)
    {
        return symbolicNameMapping.get(symbolicName);
    }

    public CompositeType getType(Class type)
    {
        return classMapping.get(type);
    }

    public static boolean isSimple(Class type)
    {
        return CollectionUtils.containsIdentity(BUILT_IN_TYPES, type) || type.isEnum();
    }

    public void register(String symbolicName, CompositeType type)
    {
        symbolicNameMapping.put(symbolicName, type);
    }
}
