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
    private Map<String, Type> properties = new HashMap<String, Type>();
    private Map<Class, List<String>> propertiesByClass = new HashMap<Class, List<String>>();

    private List<String> extensions = new LinkedList<String>();

    private Map<String, Method> setters = new HashMap<String, Method>();
    private Map<String, Method> getters = new HashMap<String, Method>();

    public CompositeType(Class type)
    {
        super(type);
    }

    public CompositeType(Class type, String symbolicName)
    {
        super(type, symbolicName);
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

    public Object instantiate(Object data) throws TypeException
    {
        if (data == null)
        {
            return null;
        }

        if (!Map.class.isAssignableFrom(data.getClass()))
        {
            throw new TypeConversionException("Expected a map type, instead received " + data.getClass());
        }

        Map<String, Object> record = (Map<String, Object>)data;

        Object instance = instantiate();

        populateInstance(record, instance);

        return instance;
    }

    public Object instantiate() throws TypeConversionException
    {
        try
        {
            return getClazz().newInstance();
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

        try
        {
            Record record = new Record();
            record.setSymbolicName(getSymbolicName());
            for (Map.Entry<String, Type> entry : properties.entrySet())
            {
                String name = entry.getKey();
                Type type = entry.getValue();

                Method getter = getters.get(name);
                Object value = getter.invoke(instance);
                if (value != null)
                {
                    Object propertyValue = type.unstantiate(value);
                    if (propertyValue != null)
                    {
                        record.put(name, propertyValue);
                    }
                }
            }
            return record;
        }
        catch (Exception e)
        {
            throw new TypeConversionException(e);
        }
    }
    
    public void populateInstance(Map<String, Object> source, Object target) throws TypeException
    {
        try
        {
            for (Map.Entry<String, Type> entry : properties.entrySet())
            {
                String name = entry.getKey();
                Type type = entry.getValue();
                Method setter = setters.get(name);
                setter.invoke(target, type.instantiate(source.get(name)));
            }
        }
        catch (Exception e)
        {
            throw new TypeException(e);
        }
    }

    /**
     * This method extracts the details from the instance and populates the map, based on this
     * typs definition.
     *
     * @param source the instance from which the data is being extracted.
     * @param target the map to which the data is being copied.
     *
     * @throws TypeConversionException if there is a problem converting the data into a form appropriate
     * for the map.
     */
    public void populateMap(Object source, Map<String, Object> target) throws TypeConversionException
    {
        try
        {
            for (Map.Entry<String, Type> entry : properties.entrySet())
            {
                String name = entry.getKey();
                Type type = entry.getValue();
                if (type instanceof PrimitiveType)
                {
                    Method getter = getters.get(name);
                    Object value = getter.invoke(source);
                    if (value != null)
                    {
                        target.put(name, value);
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new TypeConversionException(e);
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