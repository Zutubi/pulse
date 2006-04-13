/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.Namespace;

import java.util.Map;
import java.util.TreeMap;

/**
 * <class-comment/>
 */
public class Resource extends Entity implements Namespace
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

    public boolean hasVersion(String value)
    {
        return versions.containsKey(value);
    }

    public ResourceVersion getVersion(String id)
    {
        return versions.get(id);
    }

    public Map<String, ResourceVersion> getVersions()
    {
        return versions;
    }

    private void setVersions(Map<String, ResourceVersion> versions)
    {
        this.versions = versions;
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

    public void addProperty(Property p)
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
}
