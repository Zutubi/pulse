package com.zutubi.prototype.type;

import com.zutubi.pulse.util.AnnotationUtils;
import com.zutubi.pulse.util.CollectionUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class TypeRegistry
{
    private static final Class[] BUILT_IN_TYPES = {Boolean.class, Boolean.TYPE, Byte.class, Byte.TYPE, Character.class, Character.TYPE, Double.class, Double.TYPE, Float.class, Float.TYPE, Integer.class, Integer.TYPE, Long.class, Long.TYPE, Short.class, Short.TYPE, String.class};

    private Map<String, Type> symbolicNameMapping = new HashMap<String, Type>();

    private Map<Class, Type> classMapping = new HashMap<Class, Type>();

    public TypeRegistry()
    {
        // setup internal built-in types.
        List<Type> builtInTypes = new LinkedList<Type>();
        builtInTypes.add(new PrimitiveType(Boolean.class, "Boolean"));
        builtInTypes.add(new PrimitiveType(Boolean.TYPE, "boolean"));
        builtInTypes.add(new PrimitiveType(Byte.class, "Byte"));
        builtInTypes.add(new PrimitiveType(Byte.TYPE, "byte"));
        builtInTypes.add(new PrimitiveType(Character.class, "Character"));
        builtInTypes.add(new PrimitiveType(Character.TYPE, "char"));
        builtInTypes.add(new PrimitiveType(Double.class, "Double"));
        builtInTypes.add(new PrimitiveType(Double.TYPE, "double"));
        builtInTypes.add(new PrimitiveType(Float.class, "Float"));
        builtInTypes.add(new PrimitiveType(Float.TYPE, "float"));
        builtInTypes.add(new PrimitiveType(Integer.class, "Integer"));
        builtInTypes.add(new PrimitiveType(Integer.TYPE, "int"));
        builtInTypes.add(new PrimitiveType(Long.class, "Long"));
        builtInTypes.add(new PrimitiveType(Long.TYPE, "long"));
        builtInTypes.add(new PrimitiveType(Short.class, "Short"));
        builtInTypes.add(new PrimitiveType(Short.TYPE, "short"));
        builtInTypes.add(new PrimitiveType(String.class, "String"));

/*
        builtInTypes.add(new MapType(HashMap.class, "mapType"));
        builtInTypes.add(new ListType(LinkedList.class, "listType"));
*/

        for (Type type : builtInTypes)
        {
            classMapping.put(type.getClazz(), type);
            symbolicNameMapping.put(type.getSymbolicName(), type);
            ((AbstractType) type).setTypeRegistry(this);
        }
    }

    public CompositeType register(Class clazz) throws TypeException
    {
        return register(null, clazz);
    }

    public CompositeType register(String symbolicName, Class clazz) throws TypeException
    {
        if (symbolicName != null && symbolicNameMapping.containsKey(symbolicName))
        {
            throw new TypeException("Symbolic name " + symbolicName + " is already in use, can not be assigned " +
                    "to a different type " + clazz.getName());
        }

        Type type = classMapping.get(clazz);
        if (type == null)
        {
            CompositeType ctype = new CompositeType(clazz, symbolicName);
            buildType(ctype);
            classMapping.put(clazz, ctype);
            type = ctype;
        }

        if (symbolicName != null)
        {
            symbolicNameMapping.put(symbolicName, type);
        }

        return (CompositeType) type;
    }

    public CompositeType register(String symbolicName, CompositeType type) throws TypeException
    {
        if (symbolicNameMapping.containsKey(symbolicName))
        {
            throw new TypeException("Symbolic name " + symbolicName + " is already in use.");
        }
        symbolicNameMapping.put(symbolicName, type);
        return type;
    }

    private CompositeType buildType(CompositeType prototype) throws TypeException
    {
        try
        {
            Class typeClass = prototype.getClazz();
            prototype.setAnnotations(Arrays.asList(typeClass.getAnnotations()));

            BeanInfo beanInfo = null;
            if (typeClass.isInterface() || typeClass == Object.class)
            {
                beanInfo = Introspector.getBeanInfo(typeClass);
            }
            else
            {
                beanInfo = Introspector.getBeanInfo(typeClass, Object.class);
            }
            
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors())
            {
                TypeProperty property = new TypeProperty();
                property.setName(descriptor.getName());
                property.setGetter(descriptor.getReadMethod());
                property.setSetter(descriptor.getWriteMethod());

                // extract annotations for this property, from the getter, setter
                property.setAnnotations(AnnotationUtils.annotationsFromProperty(descriptor));

                // analyse the java type
                java.lang.reflect.Type type = descriptor.getReadMethod().getGenericReturnType();

                if (type instanceof Class)
                {
                    Class clazz = (Class) type;
                    if (classMapping.containsKey(clazz))
                    {
                        property.setType(classMapping.get(clazz));
                    }
                    else
                    {
                        property.setType(register(clazz));
                    }
                }
                else if (type instanceof ParameterizedType)
                {
                    // have we seen this class yet?
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    Class clazz = (Class) parameterizedType.getRawType();

                    Class valueClass = null;
                    CollectionType collection = null;
                    if (List.class.isAssignableFrom(clazz))
                    {
                        valueClass = (Class) parameterizedType.getActualTypeArguments()[0];
                        collection = new ListType();
                    }
                    else if (Map.class.isAssignableFrom(clazz))
                    {
                        valueClass = (Class) parameterizedType.getActualTypeArguments()[1];
                        collection = new MapType();
                    }

                    if (collection == null)
                    {
                        continue;
                    }

                    collection.setTypeRegistry(this);
                    if (classMapping.containsKey(valueClass))
                    {
                        collection.setCollectionType(classMapping.get(valueClass));
                    }
                    else
                    {
                        collection.setCollectionType(register(valueClass));
                    }
                    property.setType(collection);
                }
                prototype.addProperty(property);
            }
            return prototype;
        }
        catch (IntrospectionException e)
        {
            throw new TypeException(e);
        }
    }

    public Type getType(String symbolicName)
    {
        return symbolicNameMapping.get(symbolicName);
    }

    public Type getType(Class type)
    {
        return classMapping.get(type);
    }

    public static boolean isSimple(Class type)
    {
        return CollectionUtils.containsIdentity(BUILT_IN_TYPES, type) || type.isEnum();
    }

}
