package com.zutubi.pulse.core.config;

import com.zutubi.pulse.core.engine.api.ResourceProperty;

import java.util.LinkedHashMap;
import java.util.Map;

public class Resource
{
    private Map<String, ResourceProperty> properties = new LinkedHashMap<String, ResourceProperty>();
 
    private String defaultVersion;
    private Map<String, ResourceVersion> versions = new LinkedHashMap<String, ResourceVersion>();

    private String name;

    public Resource()
    {

    }

    public Resource(String name)
    {
        setName(name);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean hasVersion(String value)
    {
        return versions.containsKey(value);
    }

    public ResourceVersion getVersion(String id)
    {
        return versions.get(id);
    }

    public String getDefaultVersion()
    {
        return defaultVersion;
    }

    public void setDefaultVersion(String defaultVersion)
    {
        this.defaultVersion = defaultVersion;
    }

    public Map<String, ResourceVersion> getVersions()
    {
        return versions;
    }

    public void setVersions(Map<String, ResourceVersion> versions)
    {
        this.versions = versions;
    }

    public void deleteVersion(ResourceVersion version)
    {
        if(version.getValue().equals(defaultVersion))
        {
            defaultVersion = null;
        }
        
        versions.remove(version.getValue());
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

    public void addProperty(ResourceProperty p)
    {
        String name = p.getName();
        properties.put(name, p);
    }

    public void deleteProperty(String name)
    {
        properties.remove(name);
    }

    public void add(ResourceVersion v)
    {
        versions.put(v.getValue(), v);
    }

    public int getTotalPropertyCount()
    {
        int count = properties.size();
        for(ResourceVersion v: versions.values())
        {
            count += v.getProperties().size();
        }

        return count;
    }

    public int getEmptyVersionCount()
    {
        int count = 0;
        if(properties.isEmpty())
        {
            count++;
        }

        for(ResourceVersion v: versions.values())
        {
            if(v.getProperties().isEmpty())
            {
                count++;
            }
        }

        return count;
    }
}
