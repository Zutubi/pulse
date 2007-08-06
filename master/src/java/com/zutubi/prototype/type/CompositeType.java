package com.zutubi.prototype.type;

import com.zutubi.config.annotations.Internal;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.logging.Logger;

import java.util.*;

/**
 *
 */
public class CompositeType extends AbstractType implements ComplexType
{
    private static final Logger LOG = Logger.getLogger(CompositeType.class);

    /**
     * The list of symbolic names of types that 'extend' this type.
     */
    private List<String> extensions = new LinkedList<String>();

    /**
     * Those properties that are marked with the @Internal annotation.
     */
    private Map<String, TypeProperty> internalProperties = new HashMap<String, TypeProperty>();

    private Map<String, TypeProperty> properties = new HashMap<String, TypeProperty>();

    private Map<Class, List<String>> propertiesByClass = new HashMap<Class, List<String>>();
    private static final String XML_RPC_SYMBOLIC_NAME = "meta.symbolicName";


    public CompositeType(Class type, String symbolicName)
    {
        super(type, symbolicName);
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
            if(!(property.getType() instanceof PrimitiveType))
            {
                throw new TypeException("Internal property '" + property.getName() + "' has non-primitive type");
            }

            internalProperties.put(property.getName(), property);
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
        return CollectionUtils.map(getProperties(type), new Mapping<TypeProperty, String>()
        {
            public String map(TypeProperty property)
            {
                return property.getName();
            }
        });
    }

    public List<Type> getPropertyTypes(Class<? extends Type> type)
    {
        return CollectionUtils.map(getProperties(type), new Mapping<TypeProperty, Type>()
        {
            public Type map(TypeProperty property)
            {
                return property.getType();
            }
        });
    }

    public List<String> getPropertyNames()
    {
        return Collections.unmodifiableList(new LinkedList<String>(properties.keySet()));
    }

    public boolean hasProperty(String propertyName)
    {
        return properties.containsKey(propertyName);
    }

    public boolean hasInternalProperties()
    {
        return internalProperties.size() > 0;
    }
    
    public List<String> getInternalPropertyNames()
    {
        return Collections.unmodifiableList(new LinkedList<String>(internalProperties.keySet()));
    }

    public List<TypeProperty> getInternalProperties()
    {
        return Collections.unmodifiableList(new LinkedList<TypeProperty>(internalProperties.values()));
    }

    public void addExtension(String symbolicName)
    {
        this.extensions.add(symbolicName);
    }

    public List<String> getExtensions()
    {
        return Collections.unmodifiableList(extensions);
    }

    public void setExtensions(List<String> extensions)
    {
        this.extensions = extensions;
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
                    instance = (Configuration) getClazz().newInstance();
                }
                catch (Exception e)
                {
                    throw new TypeException(e.getMessage(), e);
                }
            }
            else
            {
                CompositeType type = typeRegistry.getType(record.getSymbolicName());
                instance = type.instantiate(data, instantiator);
            }
        }

        return instance;
    }

    public void initialise(Object instance, Object data, Instantiator instantiator)
    {
        Type actualType = typeRegistry.getType(instance.getClass());
        if(this == actualType)
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

    public MutableRecord unstantiate(Object instance) throws TypeException
    {
        MutableRecord result;

        if (extensions.size() > 0)
        {
            CompositeType actualType = typeRegistry.getType(instance.getClass());
            return actualType.unstantiate(instance);
        }
        else
        {
            result = newRecord();
        }

        for (TypeProperty property : properties.values())
        {
            unstantiateProperty(property, instance, result);
        }
        for (TypeProperty property : internalProperties.values())
        {
            unstantiateProperty(property, instance, result);
        }

        return result;
    }

    public Object toXmlRpc(Object data) throws TypeException
    {
        if(data == null)
        {
            return null;
        }
        else
        {
            typeCheck(data, Record.class);

            Record record = (Record) data;
            if(getSymbolicName().equals(record.getSymbolicName()))
            {
                Hashtable<String, Object> result = new Hashtable<String, Object>();
                result.put(XML_RPC_SYMBOLIC_NAME, getSymbolicName());
                
                for (Map.Entry<String, TypeProperty> entry : properties.entrySet())
                {
                    propertyToXmlRpc(entry, record, result);
                }
                for (Map.Entry<String, TypeProperty> entry : internalProperties.entrySet())
                {
                    propertyToXmlRpc(entry, record, result);
                }

                return result;
            }
            else
            {
                // Actually a derived type
                CompositeType actualType = typeRegistry.getType(record.getSymbolicName());
                return actualType.toXmlRpc(data);
            }
        }
    }

    private void propertyToXmlRpc(Map.Entry<String, TypeProperty> entry, Record record, Hashtable<String, Object> result) throws TypeException
    {
        Object propertyValue = record.get(entry.getKey());
        if(propertyValue != null)
        {
            result.put(entry.getKey(), entry.getValue().getType().toXmlRpc(propertyValue));
        }
    }

    public Object fromXmlRpc(Object data) throws TypeException
    {
        typeCheck(data, Hashtable.class);

        Hashtable rpcForm = (Hashtable) data;
        Object o = rpcForm.get(XML_RPC_SYMBOLIC_NAME);
        if(o == null)
        {
            throw new TypeException("No symbolic name found in XML-RPC struct");
        }

        typeCheck(o, String.class);

        String symbolicName = (String) o;
        if(symbolicName.equals(getSymbolicName()))
        {
            // Check that we recognise all of the properties given.
            for(Object key: rpcForm.keySet())
            {
                typeCheck(key, String.class);
                String keyString = (String) key;
                if(!recognisedProperty(keyString))
                {
                    throw new TypeException("Unrecognised property '" + keyString + "' for type '" + symbolicName + "'");
                }
            }

            MutableRecord result = newRecord();
            for (TypeProperty property : properties.values())
            {
                propertyFromXmlRpc(property, rpcForm, result);
            }
            for (TypeProperty property : internalProperties.values())
            {
                propertyFromXmlRpc(property, rpcForm, result);
            }

            return result;
        }
        else
        {
            // Actually a derived type
            CompositeType actualType = typeRegistry.getType(symbolicName);
            if(actualType == null)
            {
                throw new TypeException("XML-RPC struct has unrecognised symbolic name '" + symbolicName + "'");
            }

            return actualType.fromXmlRpc(data);
        }
    }

    private boolean recognisedProperty(String property)
    {
        return property.equals(XML_RPC_SYMBOLIC_NAME) || properties.containsKey(property) || internalProperties.containsKey(property);
    }

    private void propertyFromXmlRpc(TypeProperty property, Hashtable rpcForm, MutableRecord result) throws TypeException
    {
        Object value = rpcForm.get(property.getName());
        if(value != null)
        {
            try
            {
                result.put(property.getName(), property.getType().fromXmlRpc(value));
            }
            catch (TypeException e)
            {
                throw new TypeException("Converting property '" + property.getName() + "' of type '" + getSymbolicName() + "': " + e.getMessage(), e);
            }
        }
    }

    private void unstantiateProperty(TypeProperty property, Object instance, MutableRecord result) throws TypeException
    {
        try
        {
            Object value = property.getValue(instance);
            if (value != null)
            {
                result.put(property.getName(), property.getType().unstantiate(value));
            }
        }
        catch (Exception e)
        {
            throw new TypeException("Unable to invoke getter for property '" + property.getName() + "': " + e.getMessage(), e);
        }
    }

    public String getSavePath(String path, Record record)
    {
        return path;
    }

    public String getInsertionPath(String path, Record record)
    {
        return path;
    }

    public MutableRecord createNewRecord(boolean applyDefaults)
    {
        try
        {
            if (applyDefaults)
            {
                Object defaultInstance = getClazz().newInstance();
                return unstantiate(defaultInstance);
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
        if (extensions.size() > 0)
        {
            // Can only be created when the extension type is specified,
            // there is no default initialisation.
            return null;
        }

        MutableRecordImpl record = new MutableRecordImpl();
        record.setSymbolicName(getSymbolicName());

        // Create empty collections for all non-simple collection properties
        for (TypeProperty property : getProperties(CollectionType.class))
        {
            CollectionType type = (CollectionType) property.getType();
            if(type.getCollectionType() instanceof ComplexType)
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
        if(value == null || getExtensions().size() == 0)
        {
            return this;
        }
        else
        {
            return typeRegistry.getType(((Record)value).getSymbolicName());
        }
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

    public Type getPropertyType(String key)
    {
        return properties.get(key).getType();
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
}