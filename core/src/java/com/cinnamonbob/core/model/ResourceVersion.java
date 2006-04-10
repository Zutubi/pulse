package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.Namespace;

import java.util.Map;
import java.util.TreeMap;

/**
 * <class-comment/>
 */
public class ResourceVersion extends Entity implements Namespace
{
    private String value;
    private Map<String, Property> properties = new TreeMap<String, Property>();

    public ResourceVersion()
    {

    }

    public ResourceVersion(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public Map<String, Property> getProperties()
    {
        return properties;
    }

    private void setProperties(Map<String, Property> properties)
    {
        this.properties = properties;
    }

    public boolean hasProperty(String name)
    {
        return properties.containsKey(name);
    }

    public Property getProperty(String name)
    {
        return properties.get(name);
    }

    public void addProperty(Property p) throws FileLoadException
    {
        String name = p.getName();
        if (hasProperty(name))
        {
            throw new FileLoadException("Property with name '" + name + "' already exists with value '" + properties.get(name).getValue() + "'");
        }
        properties.put(name, p);
    }

    public void deleteProperty(String name)
    {
        properties.remove(name);
    }
}
