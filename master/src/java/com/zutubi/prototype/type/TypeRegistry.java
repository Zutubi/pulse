package com.zutubi.prototype.type;

import com.zutubi.config.annotations.*;
import com.zutubi.prototype.config.ConfigurationReferenceManager;
import com.zutubi.prototype.type.record.HandleAllocator;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.AnnotationUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.ReflectionUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 *
 *
 */
public class TypeRegistry
{
    private static final Class[] BUILT_IN_TYPES = {Boolean.class, Boolean.TYPE, Byte.class, Byte.TYPE, Character.class, Character.TYPE, Double.class, Double.TYPE, Float.class, Float.TYPE, Integer.class, Integer.TYPE, Long.class, Long.TYPE, Short.class, Short.TYPE, String.class};

    private Map<String, CompositeType> symbolicNameMapping = new HashMap<String, CompositeType>();

    private Map<Class, CompositeType> classMapping = new HashMap<Class, CompositeType>();
    private Map<Class, SimpleType> primitiveMapping = new HashMap<Class, SimpleType>();

    private ConfigurationReferenceManager configurationReferenceManager;
    private HandleAllocator handleAllocator;

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

    public <T extends Configuration> CompositeType register(Class<T> clazz) throws TypeException
    {
        return register(clazz, null);
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Configuration> CompositeType register(Class<T> clazz, TypeHandler handler) throws TypeException
    {
        SymbolicName symbolicName = clazz.getAnnotation(SymbolicName.class);
        if (symbolicName != null)
        {
            return register(symbolicName.value(), clazz, handler);
        }
        // this is invalid, let the base register method handle the exception generation.
        return register(null, clazz, handler);
    }

    private CompositeType register(String symbolicName, Class clazz, TypeHandler handler) throws TypeException
    {
        try
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
                type.setTypeRegistry(this);
                classMapping.put(clazz, type);

                try
                {
                    buildType(type, handler);
                    if(handler != null)
                    {
                        handler.handle(type);
                    }
                }
                catch (RuntimeException e)
                {
                    classMapping.remove(clazz);
                    throw e;
                }

                checkForExtensionParent(type);
            }

            if (symbolicName != null)
            {
                symbolicNameMapping.put(symbolicName, type);
            }

            return type;
        }
        catch (TypeException e)
        {
            throw new TypeException("Registering class '" + clazz.getName() + "': " + e.getMessage(), e);
        }
    }

    private void checkForExtensionParent(CompositeType type) throws TypeException
    {
        if(!type.isExtendable())
        {
            for(Class superClass: ReflectionUtils.getSupertypes(type.getClazz(), Object.class, true))
            {
                CompositeType superType = getType(superClass);
                if(superType != null && superType.isExtendable())
                {
                    if(type.hasAnnotation(Internal.class))
                    {
                        superType.addInternalExtension(type);
                    }
                    else
                    {
                        superType.addExtension(type);
                    }
                }
            }
        }
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

    private CompositeType buildType(CompositeType prototype, TypeHandler handler) throws TypeException
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
                Method readMethod = descriptor.getReadMethod();
                if(readMethod == null)
                {
                    // We only hook up readable properties
                    continue;
                }

                DefinedTypeProperty property = new DefinedTypeProperty();
                property.setName(descriptor.getName());
                property.setGetter(readMethod);
                property.setSetter(descriptor.getWriteMethod());

                // extract annotations for this property, from the getter, setter
                property.setAnnotations(AnnotationUtils.annotationsFromProperty(descriptor, true));

                if(property.getAnnotation(Transient.class) != null)
                {
                    continue;
                }
                
                // analyse the java type
                java.lang.reflect.Type type = readMethod.getGenericReturnType();

                if (type instanceof Class)
                {
                    Class clazz = (Class) type;
                    SimpleType simpleType = getSimpleType(clazz);
                    if (simpleType != null)
                    {
                        property.setType(simpleType);
                    }
                    else
                    {
                        CompositeType compositeType = classMapping.get(clazz);
                        if (compositeType == null)
                        {
                            compositeType = register(clazz, handler);
                        }

                        property.setType(checkReferenceType(property, compositeType));
                    }
                }
                else
                {
                    if (type instanceof ParameterizedType)
                    {
                        // have we seen this class yet?
                        ParameterizedType parameterizedType = (ParameterizedType) type;
                        Class clazz = (Class) parameterizedType.getRawType();

                        Class valueClass;
                        CollectionType propertyType = null;
                        Type collectionType;
                        if (List.class.isAssignableFrom(clazz))
                        {
                            valueClass = (Class) parameterizedType.getActualTypeArguments()[0];
                        }
                        else if (Map.class.isAssignableFrom(clazz))
                        {
                            valueClass = (Class) parameterizedType.getActualTypeArguments()[1];
                        }
                        else
                        {
                            continue;
                        }

                        if (classMapping.containsKey(valueClass))
                        {
                            collectionType = checkReferenceType(property, classMapping.get(valueClass));
                        }
                        else
                        {
                            SimpleType simpleType = getSimpleType(valueClass);
                            if (simpleType != null)
                            {
                                collectionType = simpleType;
                            }
                            else
                            {
                                CompositeType compositeType = register(valueClass, handler);
                                collectionType = checkReferenceType(property, compositeType);
                            }
                        }

                        if (List.class.isAssignableFrom(clazz))
                        {
                            propertyType = new ListType(handleAllocator, collectionType, this);
                        }
                        else
                        {
                            propertyType = new MapType(collectionType, this);
                        }

                        if(property.getAnnotation(Ordered.class) != null)
                        {
                            propertyType.setOrdered(true);
                        }

                        property.setType(propertyType);
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

    private Type checkReferenceType(TypeProperty property, CompositeType compositeType) throws TypeException
    {
        if (property.getAnnotation(Reference.class) != null)
        {
            ReferenceType referenceType = new ReferenceType(compositeType, configurationReferenceManager);
            referenceType.setTypeRegistry(this);
            return referenceType;
        }
        else
        {
            return compositeType;
        }
    }

    @SuppressWarnings({"unchecked"})
    private SimpleType getSimpleType(Class clazz)
    {
        SimpleType type = primitiveMapping.get(clazz);
        if (type == null)
        {
            if (clazz.isEnum())
            {
                type = new EnumType(clazz);
                primitiveMapping.put(clazz, type);
            }
        }

        return type;
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

    public void setConfigurationReferenceManager(ConfigurationReferenceManager configurationReferenceManager)
    {
        this.configurationReferenceManager = configurationReferenceManager;
    }

    public void setHandleAllocator(HandleAllocator handleAllocator)
    {
        this.handleAllocator = handleAllocator;
    }
}
