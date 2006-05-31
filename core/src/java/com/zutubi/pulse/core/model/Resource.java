/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core.model;

import java.util.Map;
import java.util.TreeMap;

/**
 * <class-comment/>
 */
public class Resource extends Entity
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

    public ResourceVersion getVersion(long id)
    {
        for(ResourceVersion v: versions.values())
        {
            if(v.getId() == id)
            {
                return v;
            }
        }

        return null;
    }

    public Map<String, ResourceVersion> getVersions()
    {
        return versions;
    }

    protected void setVersions(Map<String, ResourceVersion> versions)
    {
        this.versions = versions;
    }

    public void deleteVersion(ResourceVersion version)
    {
        versions.remove(version.getValue());
    }

    public Map<String, Property> getProperties()
    {
        return properties;
    }

    protected void setProperties(Map<String, Property> properties)
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
