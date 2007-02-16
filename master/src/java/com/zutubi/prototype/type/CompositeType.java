package com.zutubi.prototype.type;

import com.zutubi.prototype.type.record.Record;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class CompositeType extends AbstractType implements Traversable, Type
{
    private String symbolicName;

    private Map<String, Type> properties = new HashMap<String, Type>();
    private Map<Class, List<String>> propertiesByClass = new HashMap<Class, List<String>>();

    private List<String> extensions = new LinkedList<String>();

    private Map<String, Method> setters = new HashMap<String, Method>();
    private Map<String, Method> getters = new HashMap<String, Method>();

    private Class clazz;

    public CompositeType(Class type)
    {
        this(type, null);
    }

    public CompositeType(Class type, String symbolicName)
    {
        this.symbolicName = symbolicName;
        this.clazz = type;
    }

    public Class getClazz()
    {
        return clazz;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public void addProperty(String name, Type type, Method setter, Method getter)
    {
        properties.put(name, type);

        if (!propertiesByClass.containsKey(type.getClass()))
        {
            propertiesByClass.put(type.getClass(), new LinkedList<String>());
        }
        List<String> props = propertiesByClass.get(type.getClass());
        props.add(name);

        setters.put(name, setter);
        getters.put(name, getter);
    }

    public List<String> getProperties()
    {
        return Collections.unmodifiableList(new LinkedList<String>(properties.keySet()));
    }

    public Type getProperty(String propertyName)
    {
        return properties.get(propertyName);
    }

    /**
     * Retrieve all of the property names of properties of a specific type.
     *
     * @param type being retrieved.
     *
     * @return a list of property names, or an empty list if no properties of the requested type can
     * be located.
     */
    public List<String> getProperties(Class<? extends Type> type)
    {
        List<String> list = propertiesByClass.get(type);
        if (list != null)
        {
            return Collections.unmodifiableList(list);
        }
        return Collections.EMPTY_LIST;
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

    public Object instantiate(Object data) throws TypeConversionException
    {
        Record record = (Record) data;
        if (record == null)
        {
            return null;
        }
        try
        {
            Object instance = getClazz().newInstance();
            populateInstance(record, instance);
            return instance;
        }
        catch (TypeConversionException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new TypeConversionException(e);
        }
    }

    public void populateInstance(Map map, Object instance) throws TypeException
    {
        try
        {
            for (Map.Entry<String, Type> entry : properties.entrySet())
            {
                String name = entry.getKey();
                Type type = entry.getValue();

                Object value = type.instantiate(map.get(name));
                Method setter = setters.get(name);
                setter.invoke(instance, value);
            }
        }
        catch (Exception e)
        {
            throw new TypeConversionException(e);
        }
    }

    public Record unstantiate(Object instance) throws TypeConversionException
    {
        if (instance == null)
        {
            return null;
        }
        Record record = new Record();
        populateRecord(instance, record);
        return record;
    }

    public void populateRecord(Object instance, Record record) throws TypeConversionException
    {
        record.putMetaProperty("symbolicName", symbolicName);
        populateMap(instance, record);
    }

    public void populateMap(Object instance, Map<String, Object> map) throws TypeConversionException
    {
        try
        {
            for (Map.Entry<String, Type> entry : properties.entrySet())
            {
                String name = entry.getKey();
                Type type = entry.getValue();

                Method getter = getters.get(name);
                Object value = getter.invoke(instance);

                map.put(name, type.unstantiate(value));
            }
        }
        catch (Exception e)
        {
            throw new TypeConversionException();
        }
    }

    public Type getType(List<String> path)
    {
        if (path.size() == 0)
        {
            return this;
        }

        String propertyName = path.get(0);
        Type type = getProperty(propertyName);
        if (type instanceof CompositeType)
        {
            CompositeType ctype = (CompositeType) type;
            return ctype.getType(path.subList(1, path.size()));
        }

        if (path.size() == 1)
        {
            return type;
        }
        // we have a non - composite type yet we are trying to navigate into it. No can do.
        throw new IllegalArgumentException("Invalid path");
    }
}