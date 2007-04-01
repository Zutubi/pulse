package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.MutableRecordImpl;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.util.Mapping;

import java.lang.reflect.Method;
import java.util.*;

/**
 *
 *
 */
public class CompositeType extends AbstractType implements ComplexType
{
    private Map<Class, List<String>> propertiesByClass = new HashMap<Class, List<String>>();

    private List<String> extensions = new LinkedList<String>();

    private Map<String, TypeProperty> properties = new HashMap<String, TypeProperty>();

    public CompositeType(Class type)
    {
        super(type);
    }

    public CompositeType(Class type, String symbolicName)
    {
        super(type, symbolicName);
    }

    public void addProperty(TypeProperty property)
    {
        this.properties.put(property.getName(), property);
        Class typeClass = property.getType().getClass();
        if (!propertiesByClass.containsKey(typeClass))
        {
            propertiesByClass.put(typeClass, new LinkedList<String>());
        }
        List<String> props = propertiesByClass.get(typeClass);
        props.add(property.getName());

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
        List<String> list = propertiesByClass.get(type);
        if (list != null)
        {
            List<TypeProperty> properties = new LinkedList<TypeProperty>();
            for (String propertyName : list)
            {
                properties.add(getProperty(propertyName));
            }
            return properties;
        }
        return Collections.EMPTY_LIST;
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

    public Object instantiate(Object data) throws TypeException
    {
        try
        {
            if (data == null)
            {
                return null;
            }

            // TODO: the data parameter for this method should be a Record.
            Record record = (Record) data;

            Object instance = getClazz().newInstance();

            for (Map.Entry<String, TypeProperty> entry : properties.entrySet())
            {
                String name = entry.getKey();
                if (!record.containsKey(name))
                {
                    continue;
                }

                TypeProperty property = entry.getValue();
                Method setter = property.getSetter();
                if (setter != null)
                {
                    Type type = property.getType();
                    setter.invoke(instance, type.instantiate(record.get(name)));
                }
            }

            return instance;
        }
        catch (Exception e)
        {
            throw new TypeConversionException(e);
        }
    }

    public String insert(String path, Record newRecord, RecordManager recordManager)
    {
        recordManager.insert(path, newRecord);
        return path;
    }

    public MutableRecord createNewRecord()
    {
        MutableRecordImpl record = new MutableRecordImpl();
        for (String propertyName : getPropertyNames(MapType.class))
        {
            record.put(propertyName, new MutableRecordImpl());
        }
        for (String propertyName : getPropertyNames(ListType.class))
        {
            record.put(propertyName, new MutableRecordImpl());
        }
        record.setSymbolicName(getSymbolicName());
        return record;
    }

    public boolean isTemplated()
    {
        return false;
    }
}