package com.zutubi.prototype.type;

import com.zutubi.pulse.prototype.record.SymbolicName;
import com.zutubi.pulse.util.AnnotationUtils;
import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.prototype.annotation.Reference;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;

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

    private Map<String, CompositeType> symbolicNameMapping = new HashMap<String, CompositeType>();

    private Map<Class, CompositeType> classMapping = new HashMap<Class, CompositeType>();
    private Map<Class, PrimitiveType> primitiveMapping = new HashMap<Class, PrimitiveType>();

    private ConfigurationPersistenceManager configurationPersistenceManager;

    public TypeRegistry()
    {
        // setup internal built-in types.
        List<PrimitiveType> builtInTypes = new LinkedList<PrimitiveType>();
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

        for (PrimitiveType type : builtInTypes)
        {
            primitiveMapping.put(type.getClazz(), type);
            type.setTypeRegistry(this);
        }
    }

    public CompositeType register(Class clazz) throws TypeException
    {
        SymbolicName symbolicName = (SymbolicName) clazz.getAnnotation(SymbolicName.class);
        if (symbolicName != null)
        {
            return register(symbolicName.value(), clazz);
        }
        // this is invalid, let the base register method handle the exception generation.
        return register(null, clazz);
    }

    public CompositeType register(String symbolicName, Class clazz) throws TypeException
    {
        if (symbolicName == null)
        {
            throw new TypeException("Class " + clazz.getName() + " requires a symbolic name before it can be registered.  You can " +
                    "add a symbolic name to the class by using the @SymbolicName annotation.");
        }
        if (symbolicName != null && symbolicNameMapping.containsKey(symbolicName))
        {
            Class existingRegistration = symbolicNameMapping.get(symbolicName).getClazz();
            if (existingRegistration != clazz)
            {
                throw new TypeException("Symbolic name " + symbolicName + " is already in use, can not be assigned " +
                        "to a different type " + clazz.getName());
            }
        }

        CompositeType type = classMapping.get(clazz);
        if (type == null)
        {
            type = new CompositeType(clazz, symbolicName);
            classMapping.put(clazz, type);

            try
            {
                buildType(type);
            }
            catch (RuntimeException e)
            {
                classMapping.remove(clazz);
                throw e;
            }
        }

        if (symbolicName != null)
        {
            symbolicNameMapping.put(symbolicName, type);
        }

        return type;
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

            BeanInfo beanInfo;
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
                    if (primitiveMapping.containsKey(clazz))
                    {
                        property.setType(primitiveMapping.get(clazz));
                    }
                    else
                    {
                        CompositeType compositeType = classMapping.get(clazz);
                        if(compositeType == null)
                        {
                            compositeType = register(clazz);
                        }

                        if(property.getAnnotation(Reference.class) != null)
                        {
                            ReferenceType referenceType = new ReferenceType(compositeType, configurationPersistenceManager);
                            referenceType.setTypeRegistry(this);
                            property.setType(referenceType);
                        }
                        else
                        {
                            property.setType(compositeType);
                        }
                    }
                }
                else
                {
                    if (type instanceof ParameterizedType)
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
                        else
                        {
                            if (Map.class.isAssignableFrom(clazz))
                            {
                                valueClass = (Class) parameterizedType.getActualTypeArguments()[1];
                                collection = new MapType();
                            }
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
                            if (primitiveMapping.containsKey(valueClass))
                            {
                                property.setType(primitiveMapping.get(valueClass));
                            }
                            else
                            {
                                collection.setCollectionType(register(valueClass));
                            }
                        }
                        property.setType(collection);
                    }
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

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
