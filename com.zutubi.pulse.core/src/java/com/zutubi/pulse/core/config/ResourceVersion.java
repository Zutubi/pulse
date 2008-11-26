package com.zutubi.pulse.core.config;

import com.zutubi.pulse.core.engine.api.FileLoadException;
import com.zutubi.pulse.core.engine.api.ResourceProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 */
public class ResourceVersion
{
    private String value;
    private Map<String, ResourceProperty> properties = new LinkedHashMap<String, ResourceProperty>();

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

    public Map<String, ResourceProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, ResourceProperty> properties)
    {
        this.properties = properties;
    }

    public boolean hasProperty(String name)
    {
        return properties.containsKey(name);
    }

    public ResourceProperty getProperty(String name)
    {
        return properties.get(name);
    }

    public void addProperty(ResourceProperty p) throws FileLoadException
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
