package com.zutubi.prototype.type;

import com.zutubi.config.annotations.Internal;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.PathUtils;
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

    private ConfigurationTemplateManager configurationTemplateManager;

    public CompositeType(Class type, String symbolicName, ConfigurationTemplateManager configurationTemplateManager)
    {
        super(type, symbolicName);
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void addProperty(TypeProperty property)
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

    public Object instantiate(String path, Object data) throws TypeException
    {
        Object instance = path == null ? null : configurationTemplateManager.getInstance(path);
        if (instance == null && data != null)
        {
            try
            {
                Record record = (Record) data;

                // Check if it is actually a derived type.
                if (!getSymbolicName().equals(record.getSymbolicName()))
                {
                    CompositeType type = typeRegistry.getType(record.getSymbolicName());
                    return type.instantiate(path, data);
                }

                instance = getClazz().newInstance();
                if (path != null)
                {
                    // paths are associated with configuration objects only.
                    configurationTemplateManager.putInstance(path, instance);
                    if (instance instanceof Configuration)
                    {
                        Configuration config = (Configuration) instance;
                        config.setConfigurationPath(path);
                        config.setHandle(record.getHandle());
                    }
                }

                TypeConversionException exception = null;

                for (Map.Entry<String, TypeProperty> entry : properties.entrySet())
                {
                    exception = instantiateProperty(entry, path, record, instance, exception);
                }
                for (Map.Entry<String, TypeProperty> entry : internalProperties.entrySet())
                {
                    exception = instantiateProperty(entry, path, record, instance, exception);
                }

                if (exception != null)
                {
                    throw exception;
                }
            }
            catch (TypeConversionException e)
            {
                // let the type conversion exception pass through.
                throw e;
            }
            catch (Exception e)
            {
                throw new TypeConversionException("Instantiating type '" + getSymbolicName() + "': " + e.getMessage(), e);
            }
        }

        return instance;
    }

    private TypeConversionException instantiateProperty(Map.Entry<String, TypeProperty> entry, String path, Record record, Object instance, TypeConversionException exception) throws TypeException
    {
        String name = entry.getKey();
        TypeProperty property = entry.getValue();
        try
        {
            if (!record.containsKey(name))
            {
                if (property.getType() instanceof CollectionType)
                {
                    // Be nice and create empty collection instances instead of
                    // leaving nulls.
                    property.setValue(instance, ((CollectionType) property.getType()).emptyInstance());
                }

                return exception;
            }

            // Instantiate even if there is no setter so the instance
            // is both checked for validity and cached.
            Type type = property.getType();
            Object value = type.instantiate(path == null ? null : PathUtils.getPath(path, name), record.get(name));
            property.setValue(instance, value);
        }
        catch (Exception e)
        {
            if (exception == null)
            {
                exception = new TypeConversionException(e);
            }
            exception.addFieldError(name, e.getMessage());
        }
        return exception;
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