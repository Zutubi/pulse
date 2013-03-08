package com.zutubi.tove.type;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.zutubi.tove.annotations.ExternalState;
import com.zutubi.tove.annotations.Internal;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;
import com.zutubi.util.GraphFunction;
import com.zutubi.util.logging.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.newArrayList;

/**
 * A composite represents a user-defined Java type, i.e. a class.  Composites
 * have properties based on the Java bean properties of the class, along with
 * (in some cases) extension properties which are not defined directly on the
 * class but are added at run time.
 */
public class CompositeType extends AbstractType implements ComplexType
{
    private static final Logger LOG = Logger.getLogger(CompositeType.class);

    public static final String XML_RPC_SYMBOLIC_NAME = "meta.symbolicName";

    /**
     * An instance of this type created via the default constructor.
     */
    private Configuration defaultInstance;
    private List<Annotation> annotations = new LinkedList<Annotation>();
    /**
     * If this type extends another, the type it extends (else null).
     */
    private List<CompositeType> superTypes = new LinkedList<CompositeType>();
    /**
     * The list of types that 'extend' this type.
     */
    private List<CompositeType> extensions = new LinkedList<CompositeType>();
    /**
     * Extensions that are created internally but not via UIs.
     */
    private List<CompositeType> internalExtensions = new LinkedList<CompositeType>();

    /**
     * Those properties that are marked with the @Internal annotation.
     */
    private Map<String, TypeProperty> internalProperties = new HashMap<String, TypeProperty>();

    private TypeProperty externalStateProperty = null;

    private Map<String, TypeProperty> properties = new HashMap<String, TypeProperty>();

    private Map<Class, List<String>> propertiesByClass = new HashMap<Class, List<String>>();

    public CompositeType(Class<? extends Configuration> type, String symbolicName) throws TypeException
    {
        super(type, symbolicName);
        if (!Modifier.isAbstract(type.getModifiers()))
        {
            try
            {
                defaultInstance = type.newInstance();
            }
            catch (Exception e)
            {
                throw new TypeException("Cannot instantiate class '" + type.getName() + "' (is there a public default constructor): " + e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public Class<? extends Configuration> getClazz()
    {
        return (Class<? extends Configuration>) super.getClazz();
    }

    public Configuration getDefaultInstance()
    {
        return defaultInstance;
    }

    public void addProperty(TypeProperty property) throws TypeException
    {
        if (property.getAnnotation(Internal.class) == null)
        {
            properties.put(property.getName(), property);
            Class typeClass = property.getType().getClass();
            if (!propertiesByClass.containsKey(typeClass))
            {
                propertiesByClass.put(typeClass, new LinkedList<String>());
            }
            List<String> props = propertiesByClass.get(typeClass);
            props.add(property.getName());
        }
        else
        {
            if (!isSimpleProperty(property.getType()))
            {
                throw new TypeException("Internal property '" + property.getName() + "' has non-simple type");
            }

            internalProperties.put(property.getName(), property);

            if(property.getAnnotation(ExternalState.class) != null)
            {
                if(property.getType().getClazz() != long.class)
                {
                    throw new TypeException("External state property '" + property.getName() + "' is not of type long");
                }

                if(externalStateProperty != null)
                {
                    throw new TypeException("Type has two external state properties '" + externalStateProperty.getName() + "' and '" + property.getName() + "'");
                }

                externalStateProperty = property;
            }
        }

    }

    public List<TypeProperty> getProperties()
    {
        return Collections.unmodifiableList(new LinkedList<TypeProperty>(properties.values()));
    }

    public TypeProperty getProperty(String propertyName)
    {
        return properties.get(propertyName);
    }

    /**
     * Retrieve all of the property names of properties of a specific type.
     *
     * @param type being retrieved.
     * @return a list of property names, or an empty list if no properties of the requested type can
     *         be located.
     */
    public List<TypeProperty> getProperties(Class<? extends Type> type)
    {
        List<TypeProperty> result = new LinkedList<TypeProperty>();
        for (Map.Entry<Class, List<String>> entry : propertiesByClass.entrySet())
        {
            if (type.isAssignableFrom(entry.getKey()))
            {
                for (String property : entry.getValue())
                {
                    result.add(getProperty(property));
                }
            }
        }
        return result;
    }

    public List<String> getPropertyNames(Class<? extends Type> type)
    {
        return newArrayList(transform(getProperties(type), new Function<TypeProperty, String>()
        {
            public String apply(TypeProperty property)
            {
                return property.getName();
            }
        }));
    }

    public List<String> getPropertyNames()
    {
        return new LinkedList<String>(properties.keySet());
    }

    /**
     * @return the names of all embedded properties, that is simple values
     *         and simple collections.
     */
    public List<String> getSimplePropertyNames()
    {
        List<String> result = new LinkedList<String>();
        for (Map.Entry<String, TypeProperty> entry : properties.entrySet())
        {
            if (isSimpleProperty(entry.getValue().getType()))
            {
                result.add(entry.getKey());
            }
        }

        return result;
    }

    /**
     * @return the names of all non-embedded properties, that is nested
     *         composites and complex collections.
     */
    public List<String> getNestedPropertyNames()
    {
        List<String> result = new LinkedList<String>();
        for (Map.Entry<String, TypeProperty> entry : properties.entrySet())
        {
            if (!isSimpleProperty(entry.getValue().getType()))
            {
                result.add(entry.getKey());
            }
        }

        return result;
    }

    private boolean isSimpleProperty(Type propertyType)
    {
        return propertyType instanceof SimpleType || propertyType instanceof CollectionType && propertyType.getTargetType() instanceof SimpleType;
    }

    public boolean hasProperty(String propertyName)
    {
        return properties.containsKey(propertyName);
    }

    public boolean hasInternalProperty(String propertyName)
    {
        return internalProperties.containsKey(propertyName);
    }

    public TypeProperty getInternalProperty(String propertyName)
    {
        return internalProperties.get(propertyName);
    }

    public List<String> getInternalPropertyNames()
    {
        return Collections.unmodifiableList(new LinkedList<String>(internalProperties.keySet()));
    }

    public List<TypeProperty> getInternalProperties()
    {
        return Collections.unmodifiableList(new LinkedList<TypeProperty>(internalProperties.values()));
    }

    public TypeProperty getExternalStateProperty()
    {
        return externalStateProperty;
    }

    public void addAnnotation(Annotation annotation)
    {
        this.annotations.add(annotation);
    }

    public void setAnnotations(List<Annotation> annotations)
    {
        this.annotations = new LinkedList<Annotation>(annotations);
    }

    public List<Annotation> getAnnotations(boolean includeInherited)
    {
        if(includeInherited)
        {
            List<Annotation> result = new LinkedList<Annotation>(annotations);
            for (CompositeType superType: superTypes)
            {
                result.addAll(superType.getAnnotations(true));
            }
            return result;
        }
        else
        {
            return Collections.unmodifiableList(annotations);
        }
    }

    public List<Annotation> getAnnotations(final Class annotationType, boolean includeInherited)
    {
        List<Annotation> result = new LinkedList<Annotation>();
        if(includeInherited)
        {
            for(CompositeType superType: superTypes)
            {
                result.addAll(superType.getAnnotations(annotationType, true));
            }
        }

        addAll(result, filter(annotations, new Predicate<Annotation>()
        {
            public boolean apply(Annotation annotation)
            {
                return annotation.annotationType().equals(annotationType);
            }
        }));

        return result;
    }

    public <T extends Annotation> boolean hasAnnotation(Class<T> annotationType, boolean includeInherited)
    {
        return getAnnotation(annotationType, includeInherited) != null;
    }

    public <T extends Annotation> T getAnnotation(final Class<T> annotationType, boolean includeInherited)
    {
        T result = annotationType.cast(find(annotations, new Predicate<Annotation>()
        {
            public boolean apply(Annotation annotation)
            {
                return annotation.annotationType().equals(annotationType);
            }
        }, null));

        if(result == null && includeInherited)
        {
            for(CompositeType superType: superTypes)
            {
                result = superType.getAnnotation(annotationType, includeInherited);
                if(result != null)
                {
                    break;
                }
            }
        }

        return result;
    }

    public boolean isExtendable()
    {
        int modifiers = getClazz().getModifiers();
        return Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers);
    }

    public List<CompositeType> getSuperTypes()
    {
        return superTypes;
    }

    private void registerSubtype(CompositeType type, boolean internal, boolean direct) throws TypeException
    {
        if (!this.getClazz().isAssignableFrom(type.getClazz()))
        {
            throw new TypeException("Extension class '" + type.getClazz().getName() + "' is not a subtype of '" + getClazz().getName() + "'");
        }

        // All of our concrete descendants get registered on the extensions
        // list, but only if we are not concrete.
        if (isExtendable() && !type.isExtendable())
        {
            if (internal)
            {
                internalExtensions.add(type);
            }
            else
            {
                extensions.add(type);
            }
        }

        if (direct)
        {
            type.superTypes.add(this);
        }

        // Recurse up to register indirect concrete extensions with parent
        for(CompositeType superType: superTypes)
        {
            superType.registerSubtype(type, internal, false);
        }
    }

    void registerSubtype(CompositeType type) throws TypeException
    {
        registerSubtype(type, false, true);
    }

    public List<CompositeType> getExtensions()
    {
        return Collections.unmodifiableList(extensions);
    }

    void registerInternalSubtype(CompositeType type) throws TypeException
    {
        registerSubtype(type, true, true);
    }

    public List<CompositeType> getInternalExtensions()
    {
        return Collections.unmodifiableList(internalExtensions);
    }

    public Configuration instantiate(Object data, Instantiator instantiator) throws TypeException
    {
        Configuration instance = null;
        if (data != null)
        {
            Record record = (Record) data;

            // Check if it is actually a derived type.
            if (getSymbolicName().equals(record.getSymbolicName()))
            {
                try
                {
                    instance = getClazz().newInstance();
                }
                catch (Exception e)
                {
                    throw new TypeException(e.getMessage(), e);
                }
            }
            else
            {
                CompositeType type = typeRegistry.getType(record.getSymbolicName());
                if (type == null)
                {
                    throw new TypeException("Record has unrecognised symbolic name '" + record.getSymbolicName() + "': this could be caused by a missing plugin");
                }
                
                instance = type.instantiate(data, instantiator);
            }
        }

        return instance;
    }

    public void initialise(Object instance, Object data, Instantiator instantiator)
    {
        Type actualType = typeRegistry.getType(instance.getClass());
        if (this == actualType)
        {
            Record record = (Record) data;
            for (Map.Entry<String, TypeProperty> entry : properties.entrySet())
            {
                instantiateProperty(entry, record, (Configuration) instance, instantiator);
            }
            for (Map.Entry<String, TypeProperty> entry : internalProperties.entrySet())
            {
                instantiateProperty(entry, record, (Configuration) instance, instantiator);
            }
        }
        else
        {
            actualType.initialise(instance, data, instantiator);
        }
    }

    private void instantiateProperty(Map.Entry<String, TypeProperty> entry, Record record, Configuration instance, Instantiator instantiator)
    {
        String name = entry.getKey();
        try
        {
            TypeProperty property = entry.getValue();
            if (record.containsKey(name))
            {
                // Instantiate even if there is no setter so the instance
                // is both checked for validity and cached.
                Type type = property.getType();
                Object value = instantiator.instantiate(name, true, type, record.get(name));
                property.setValue(instance, value);
            }
        }
        catch (Exception e)
        {
            LOG.debug("Unable to instantiate property '" + name + "': " + e.getMessage(), e);
            instance.addFieldError(name, e.getMessage());
        }
    }

    public MutableRecord unstantiate(Object instance, String templateOwnerPath) throws TypeException
    {
        if (instance == null)
        {
            return null;
        }

        MutableRecord result;
        CompositeType actualType = typeRegistry.getType(instance.getClass());
        if (actualType == null)
        {
            throw new TypeException("Unknown (registered) type: " + instance.getClass());
        }

        if (actualType != this)
        {
            return actualType.unstantiate(instance, templateOwnerPath);
        }
        else
        {
            result = newRecord();

            for (TypeProperty property : properties.values())
            {
                unstantiateProperty(property, instance, templateOwnerPath, result);
            }
            for (TypeProperty property : internalProperties.values())
            {
                unstantiateProperty(property, instance, templateOwnerPath, result);
            }

            copyMetaToRecord(instance, result);

            return result;
        }
    }

    public Hashtable<String, Object> toXmlRpc(String templateOwnerPath, Object data) throws TypeException
    {
        if (data == null)
        {
            return null;
        }
        else
        {
            typeCheck(data, Record.class);

            Record record = (Record) data;
            if (getSymbolicName().equals(record.getSymbolicName()))
            {
                Hashtable<String, Object> result = new Hashtable<String, Object>();
                result.put(XML_RPC_SYMBOLIC_NAME, getSymbolicName());

                for (Map.Entry<String, TypeProperty> entry : properties.entrySet())
                {
                    propertyToXmlRpc(entry, templateOwnerPath, record, result);
                }

                return result;
            }
            else
            {
                // Actually a derived type
                CompositeType actualType = typeRegistry.getType(record.getSymbolicName());
                return actualType.toXmlRpc(templateOwnerPath, data);
            }
        }
    }

    private void propertyToXmlRpc(Map.Entry<String, TypeProperty> entry, String templateOwnerPath, Record record, Hashtable<String, Object> result) throws TypeException
    {
        Object propertyValue = record.get(entry.getKey());
        Object value = entry.getValue().getType().toXmlRpc(templateOwnerPath, propertyValue);
        if (value != null)
        {
            result.put(entry.getKey(), value);
        }
    }

    /**
     * Returns the type of the given XML-RPC struct in symbolic name form.
     *
     * @param rpcForm the XML-RPC struct to retrieve the type for
     * @return the symbolic name of the type of the struct
     * @throws TypeException if no symbolic name is found in the struct
     */
    public static String getTypeFromXmlRpc(Hashtable rpcForm) throws TypeException
    {
        Object o = rpcForm.get(XML_RPC_SYMBOLIC_NAME);
        if (o == null)
        {
            throw new TypeException("No symbolic name found in XML-RPC struct");
        }

        typeCheck(o, String.class);
        return (String) o;
    }

    public MutableRecord fromXmlRpc(String templateOwnerPath, Object data, boolean applyDefaults) throws TypeException
    {
        typeCheck(data, Hashtable.class);

        Hashtable rpcForm = (Hashtable) data;
        String symbolicName = getTypeFromXmlRpc(rpcForm);
        if (symbolicName.equals(getSymbolicName()))
        {
            // Check that we recognise all of the properties given.
            for (Object key : rpcForm.keySet())
            {
                typeCheck(key, String.class);
                String keyString = (String) key;
                if (!recognisedProperty(keyString))
                {
                    throw new TypeException("Unrecognised property '" + keyString + "' for type '" + symbolicName + "'");
                }
            }

            MutableRecord result = createNewRecord(applyDefaults);

            // Internal properties may not be set this way, so strip them
            // from the default config.
            for (TypeProperty property: internalProperties.values())
            {
                result.remove(property.getName());
            }
            
            for (TypeProperty property : properties.values())
            {
                propertyFromXmlRpc(templateOwnerPath, property, rpcForm, result);
            }

            return result;
        }
        else
        {
            // Actually a derived type
            CompositeType actualType = typeRegistry.getType(symbolicName);
            if (actualType == null)
            {
                throw new TypeException("XML-RPC struct has unrecognised symbolic name '" + symbolicName + "'");
            }

            return actualType.fromXmlRpc(templateOwnerPath, data, true);
        }
    }

    private boolean recognisedProperty(String property)
    {
        return property.startsWith("meta.") || properties.containsKey(property);
    }

    private void propertyFromXmlRpc(String tempalteOwnerPath, TypeProperty property, Hashtable rpcForm, MutableRecord result) throws TypeException
    {
        Object value = rpcForm.get(property.getName());
        if (value != null)
        {
            try
            {
                result.put(property.getName(), property.getType().fromXmlRpc(tempalteOwnerPath, value, true));
            }
            catch (TypeException e)
            {
                throw new TypeException("Converting property '" + property.getName() + "' of type '" + getSymbolicName() + "': " + e.getMessage(), e);
            }
        }
    }

    private void unstantiateProperty(TypeProperty property, Object instance, String templateOwnerPath, MutableRecord result) throws TypeException
    {
        try
        {
            Object value = property.getValue(instance);
            Object unstantiatedValue = property.getType().unstantiate(value, templateOwnerPath);
            if (unstantiatedValue != null)
            {
                result.put(property.getName(), unstantiatedValue);
            }
        }
        catch (Exception e)
        {
            throw new TypeException("Unable to invoke getter for property '" + property.getName() + "': " + e.getMessage(), e);
        }
    }

    public MutableRecord createNewRecord(boolean applyDefaults)
    {
        try
        {
            if (applyDefaults)
            {
                Object defaultInstance = getClazz().newInstance();
                return unstantiate(defaultInstance, null);
            }
            else
            {
                return newRecord();
            }
        }
        catch (Exception e)
        {
            LOG.severe(e);
            return null;
        }
    }

    private MutableRecord newRecord()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        record.setSymbolicName(getSymbolicName());

        // Create empty collections for all non-simple collection properties
        for (TypeProperty property : getProperties(CollectionType.class))
        {
            CollectionType type = (CollectionType) property.getType();
            if (type.getCollectionType() instanceof ComplexType)
            {
                record.put(property.getName(), type.createNewRecord(true));
            }
        }

        return record;
    }

    public boolean isTemplated()
    {
        return false;
    }

    public CompositeType getActualType(Object value)
    {
        if (value == null || extensions.size() == 0 && internalExtensions.size() == 0)
        {
            return this;
        }
        else
        {
            return typeRegistry.getType(((Record) value).getSymbolicName());
        }
    }

    public boolean deepValueEquals(Object data1, Object data2)
    {
        if(!(data1 instanceof Record))
        {
            throw new IllegalArgumentException("Expecting record, got '" + data1.getClass().getName() + "'");
        }

        if(!(data2 instanceof Record))
        {
            throw new IllegalArgumentException("Expecting record, got '" + data2.getClass().getName() + "'");
        }

        Record r1 = (Record) data1;
        Record r2 = (Record) data2;

        // Check the types match
        String symbolicName = r1.getSymbolicName();
        if(!Objects.equal(symbolicName, r2.getSymbolicName()))
        {
            return false;
        }

        // Check we are the actual type.
        if(symbolicName == null)
        {
            throw new IllegalArgumentException("Record has no symbolic name");
        }

        if(!symbolicName.equals(getSymbolicName()))
        {
            // Defer to actual type.
            CompositeType actualType = typeRegistry.getType(symbolicName);
            if(actualType == null)
            {
                throw new IllegalArgumentException("Record has unrecognised type '" + symbolicName + "'");
            }
            
            return actualType.deepValueEquals(r1, r2);
        }

        // Simple properties
        if(!r1.simpleEquals(r2))
        {
            return false;
        }

        // Nested properties
        List<String> nested1 = new ArrayList<String>(r1.nestedKeySet());
        List<String> nested2 = new ArrayList<String>(r2.nestedKeySet());
        Collections.sort(nested1);
        Collections.sort(nested2);

        if(!nested1.equals(nested2))
        {
            return false;
        }

        for(String key: nested1)
        {
            Type type = getDeclaredPropertyType(key);
            if(type == null)
            {
                throw new IllegalArgumentException("Record has unrecognised key '" + key + "'");
            }

            if(!type.deepValueEquals(r1.get(key), r2.get(key)))
            {
                return false;
            }
        }

        return true;
    }

    public Type getDeclaredPropertyType(String propertyName)
    {
        TypeProperty property = properties.get(propertyName);
        return property == null ? null : property.getType();
    }

    public Type getActualPropertyType(String propertyName, Object propertyValue)
    {
        TypeProperty property = properties.get(propertyName);
        return property == null ? null : property.getType().getActualType(propertyValue);
    }

    public boolean isValid(Object instance)
    {
        Configuration configuration = (Configuration) instance;
        CompositeType actualType = typeRegistry.getType(configuration.getClass());
        if (actualType != this)
        {
            return actualType.isValid(configuration);
        }

        if (!configuration.isValid())
        {
            return false;
        }

        for (String propertyName : getPropertyNames(ComplexType.class))
        {
            TypeProperty property = getProperty(propertyName);
            try
            {
                Object nestedInstance = property.getValue(configuration);
                if (nestedInstance != null)
                {
                    if (!((ComplexType) property.getType()).isValid(nestedInstance))
                    {
                        return false;
                    }
                }
            }
            catch (Exception e)
            {
                LOG.severe(e);
                return false;
            }
        }

        return true;
    }

    public void forEachComplex(Object instance, GraphFunction<Object> f) throws TypeException
    {
        f.process(instance);

        for (String propertyName : getPropertyNames(ComplexType.class))
        {
            TypeProperty property = getProperty(propertyName);
            try
            {
                Object nestedInstance = property.getValue(instance);
                if (nestedInstance != null)
                {
                    ComplexType nestedType = (ComplexType) property.getType();
                    f.push(propertyName);
                    nestedType.forEachComplex(nestedInstance, f);
                    f.pop();
                }
            }
            catch (Exception e)
            {
                throw new TypeException("Error getting value of property '" + propertyName + "': " + e.getMessage(), e);
            }
        }
    }

    public boolean hasSignificantKeys()
    {
        return true;
    }

    public Type getPropertyType(String key)
    {
        return properties.get(key).getType();
    }

    /**
     * Determines if the class or interface represented by this
     * <code>Type</code> object is either the same as, or is a superclass or
     * superinterface of, the class or interface represented by the specified
     * <code>Type</code> parameter. It returns <code>true</code> if so;
     * otherwise it returns <code>false</code>.
     *
     * @param t the <code>Type</code> object to be checked
     * @return the <code>boolean</code> value indicating whether objects of the
     * type <code>t</code> can be assigned to objects of this type
     */
    public boolean isAssignableFrom(Type t)
    {
        return getClazz().isAssignableFrom(t.getClazz());
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        CompositeType that = (CompositeType) o;
        return getSymbolicName().equals(that.getSymbolicName());
    }

    public int hashCode()
    {
        return getSymbolicName().hashCode();
    }

    public String toString()
    {
        return getSymbolicName();
    }
}