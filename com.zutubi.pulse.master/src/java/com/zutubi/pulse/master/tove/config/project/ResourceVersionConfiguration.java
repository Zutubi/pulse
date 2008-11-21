package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.FileLoadException;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.ID;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

@Form(fieldOrder = {"value"})
@SymbolicName("zutubi.resourceVersion")
public class ResourceVersionConfiguration  extends AbstractConfiguration
{
    @ID
    private String value;
    @Ordered
    private Map<String, ResourcePropertyConfiguration> properties = new LinkedHashMap<String, ResourcePropertyConfiguration>();

    public ResourceVersionConfiguration()
    {

    }

    public ResourceVersionConfiguration(String value)
    {
        this.value = value;
    }

    public ResourceVersionConfiguration(ResourceVersion v)
    {
        this.value = v.getValue();

        // properties
        for (ResourceProperty rp : v.getProperties().values())
        {
            ResourcePropertyConfiguration rpc = new ResourcePropertyConfiguration(rp);
            addProperty(rpc);
        }
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public Map<String, ResourcePropertyConfiguration> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, ResourcePropertyConfiguration> properties)
    {
        this.properties = properties;
    }

    public boolean hasProperty(String name)
    {
        return properties.containsKey(name);
    }

    public ResourcePropertyConfiguration getProperty(String name)
    {
        return properties.get(name);
    }

    public void addProperty(ResourcePropertyConfiguration p)
    {
        String name = p.getName();
        if (hasProperty(name))
        {
            throw new IllegalArgumentException("Property with name '" + name + "' already exists with value '" + properties.get(name).getValue() + "'");
        }
        properties.put(name, p);
    }

    public void deleteProperty(String name)
    {
        properties.remove(name);
    }

    public ResourceVersion asResourceVersion()
    {
        ResourceVersion v = new ResourceVersion(getValue());

        for (ResourcePropertyConfiguration rpc : getProperties().values())
        {
            try
            {
                v.addProperty(rpc.asResourceProperty());
            }
            catch (FileLoadException e)
            {
                // not going to happen.
            }
        }

        return v;
    }
}


