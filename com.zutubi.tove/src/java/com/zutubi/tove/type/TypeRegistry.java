/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.tove.type;

import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.ConfigurationReferenceManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.record.HandleAllocator;
import com.zutubi.util.reflection.AnnotationUtils;
import com.zutubi.util.reflection.ReflectionUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
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

    private Map<Class<? extends Configuration>, CompositeType> classMapping = new HashMap<Class<? extends Configuration>, CompositeType>();
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
        builtInTypes.add(new PrimitiveType(File.class, "File"));
        builtInTypes.add(new PrimitiveType(String.class, "String"));

        for (PrimitiveType type : builtInTypes)
        {
            primitiveMapping.put(type.getClazz(), type);
            type.setTypeRegistry(this);
        }
    }

    public CompositeType register(Class<? extends Configuration> clazz) throws TypeException
    {
        return register(clazz, null);
    }

    public CompositeType register(Class<? extends Configuration> clazz, TypeHandler handler) throws TypeException
    {
        SymbolicName symbolicName = clazz.getAnnotation(SymbolicName.class);
        if (symbolicName != null)
        {
            return register(symbolicName.value(), clazz, handler);
        }
        // this is invalid, let the base register method handle the exception generation.
        return register(null, clazz, handler);
    }

    private CompositeType register(String symbolicName, Class<? extends Configuration> clazz, TypeHandler handler) throws TypeException
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

    @SuppressWarnings("unchecked")
    private Class<? extends Configuration> asConfigurationClass(Class<?> clazz) throws TypeException
    {
        if (!Configuration.class.isAssignableFrom(clazz))
        {
            throw new TypeException("Class " + clazz.getName() + " does not implement Configuration");
        }

        return (Class<? extends Configuration>) clazz;
    }

    private void checkForExtensionParent(CompositeType type) throws TypeException
    {
        Class superClass = type.getClazz().getSuperclass();
        if(superClass != Object.class)
        {
            checkSuperType(type, superClass);
        }

        for(Class iface: type.getClazz().getInterfaces())
        {
            checkSuperType(type, iface);
        }
    }

    private void checkSuperType(CompositeType type, Class superType) throws TypeException
    {
        CompositeType candidateSuper = getType(superType);
        if (candidateSuper != null)
        {
            if(type.hasAnnotation(Internal.class, false))
            {
                candidateSuper.registerInternalSubtype(type);
            }
            else
            {
                candidateSuper.registerSubtype(type);
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

            PropertyDescriptor[] propertyDescriptors = ReflectionUtils.getBeanProperties(typeClass);
            for (PropertyDescriptor descriptor : propertyDescriptors)
            {
                DefinedTypeProperty property = new DefinedTypeProperty();
                property.setName(descriptor.getName());
                property.setGetter(descriptor.getReadMethod());
                property.setSetter(descriptor.getWriteMethod());

                if (!property.isReadable())
                {
                    // we only hook up readable properties.
                    continue;
                }

                // extract annotations for this property, from the getter, setter
                property.setAnnotations(AnnotationUtils.annotationsFromProperty(descriptor, true));

                // skip properties marked as transient.
                if(property.getAnnotation(Transient.class) != null)
                {
                    continue;
                }
                
                // analyse the java type
                java.lang.reflect.Type type = descriptor.getReadMethod().getGenericReturnType();

                if (type instanceof Class)
                {
                    Class clazz = (Class) type;
                    SimpleType simpleType = getSimpleType(clazz);
                    if (simpleType != null)
                    {
                        property.setType(simpleType);
                    }
                    else // is not a simple type.
                    {
                        CompositeType compositeType = classMapping.get(clazz);
                        if (compositeType == null) // type has not yet been registered, do so now.
                        {
                            compositeType = register(asConfigurationClass(clazz), handler);
                        }

                        property.setType(checkReferenceType(property, compositeType));
                    }
                }
                else
                {
                    if (type instanceof ParameterizedType)
                    {
                        ParameterizedType parameterizedType = (ParameterizedType) type;
                        Class clazz = (Class) parameterizedType.getRawType();

                        Class valueClass;
                        CollectionType propertyType;
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

                        // have we seen this class yet?
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
                                CompositeType compositeType = register(asConfigurationClass(valueClass), handler);
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

    public void setConfigurationReferenceManager(ConfigurationReferenceManager configurationReferenceManager)
    {
        this.configurationReferenceManager = configurationReferenceManager;
    }

    public void setHandleAllocator(HandleAllocator handleAllocator)
    {
        this.handleAllocator = handleAllocator;
    }
}
