package com.zutubi.pulse.core.config;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.ID;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.FileLoadException;

import java.util.Map;
import java.util.TreeMap;

/**
 */
@Form(fieldOrder = {"value"})
@SymbolicName("zutubi.resourceVersion")
public class ResourceVersion extends AbstractConfiguration
{
    @ID
    private String value;
    private Map<String, ResourceProperty> properties = new TreeMap<String, ResourceProperty>();

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
