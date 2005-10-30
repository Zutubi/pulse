package com.cinnamonbob.core;

import java.util.Map;
import java.util.TreeMap;

/**
 * <class-comment/>
 */
public class ResourceVersion implements Namespace
{
    private String id;
    private Map<String, Property> properties = new TreeMap<String, Property>();

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Map<String, Property> getProperties()
    {
        return properties;
    }

    public boolean hasProperty(String name)
    {
        return properties.containsKey(name);
    }

    public void addProperty(Property p) throws FileLoadException
    {
        String name = p.getName();
        if (hasProperty(name))
        {
            throw new FileLoadException("Property with name '"+name+"' already exists with value '"+properties.get(name).getValue()+"'");
        }
        properties.put(name, p);
    }
}
