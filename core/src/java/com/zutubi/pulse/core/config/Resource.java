package com.zutubi.pulse.core.config;

import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Transient;
import com.zutubi.config.annotations.Form;

import java.util.Map;
import java.util.TreeMap;

/**
 * A resource is something that is required by a build.  It may be an
 * external tool, a certain operating system, or some virtual concept.
 */
@Form(fieldOrder = { "name", "defaultVersion" })
@SymbolicName("internal.resource")
public class Resource extends AbstractNamedConfiguration
{
    private Map<String, ResourceProperty> properties = new TreeMap<String, ResourceProperty>();
    private String defaultVersion;
    private Map<String, ResourceVersion> versions = new TreeMap<String, ResourceVersion>();

    public Resource()
    {

    }

    public Resource(String name)
    {
        setName(name);
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

    @Transient
    public int getTotalPropertyCount()
    {
        int count = properties.size();
        for(ResourceVersion v: versions.values())
        {
            count += v.getProperties().size();
        }

        return count;
    }

    @Transient
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
