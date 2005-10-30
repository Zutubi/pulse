package com.cinnamonbob.core;

import java.util.Map;
import java.util.TreeMap;

/**
 * <class-comment/>
 */
public class Resource implements Namespace
{
    private String name;

    private Map<String, Property> properties = new TreeMap<String, Property>();

    private Map<String, ResourceVersion> versions = new TreeMap<String, ResourceVersion>();

    public Resource()
    {

    }

    public Resource(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public ResourceVersion getVersion(String id)
    {
        return versions.get(id);
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

    public void add(ResourceVersion v)
    {
        versions.put(v.getId(), v);
    }
}
