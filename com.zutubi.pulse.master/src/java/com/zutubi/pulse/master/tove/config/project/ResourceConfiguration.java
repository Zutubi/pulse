package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.AbstractNamedConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A resource is something that is required by a build.  It may be an
 * external tool, a certain operating system, or some virtual concept.
 */
@Form(fieldOrder = { "name", "defaultVersion" })
@Table(columns = {"name", "defaultVersion"})
@SymbolicName("zutubi.resource")
public class ResourceConfiguration  extends AbstractNamedConfiguration
{
    private Map<String, ResourcePropertyConfiguration> properties = new TreeMap<String, ResourcePropertyConfiguration>();

    @Select(optionProvider = "ResourceVersionOptionProvider")
    private String defaultVersion;
    private Map<String, ResourceVersionConfiguration> versions = new TreeMap<String, ResourceVersionConfiguration>();

    public ResourceConfiguration()
    {

    }

    public ResourceConfiguration(String name)
    {
        setName(name);
    }

    public ResourceConfiguration(Resource resource)
    {
        setName(resource.getName());
        this.defaultVersion = resource.getDefaultVersion();

        // properties
        for (String key : resource.getProperties().keySet())
        {
            ResourcePropertyConfiguration rpc = new ResourcePropertyConfiguration(resource.getProperty(key));
            properties.put(key, rpc);
        }

        // versions.
        for (String key : resource.getVersions().keySet())
        {
            ResourceVersionConfiguration rvc = new ResourceVersionConfiguration(resource.getVersion(key));
            versions.put(key, rvc);
        }
    }

    public boolean hasVersion(String value)
    {
        return versions.containsKey(value);
    }

    public ResourceVersionConfiguration getVersion(String id)
    {
        return versions.get(id);
    }

    @Wizard.Ignore
    public String getDefaultVersion()
    {
        return defaultVersion;
    }

    public void setDefaultVersion(String defaultVersion)
    {
        this.defaultVersion = defaultVersion;
    }

    public Map<String, ResourceVersionConfiguration> getVersions()
    {
        return versions;
    }

    public void setVersions(Map<String, ResourceVersionConfiguration> versions)
    {
        this.versions = versions;
    }

    public void deleteVersion(ResourceVersionConfiguration version)
    {
        if(version.getValue().equals(defaultVersion))
        {
            defaultVersion = null;
        }

        versions.remove(version.getValue());
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
        properties.put(name, p);
    }

    public void deleteProperty(String name)
    {
        properties.remove(name);
    }

    public void add(ResourceVersionConfiguration v)
    {
        versions.put(v.getValue(), v);
    }

    @Transient
    public int getTotalPropertyCount()
    {
        int count = properties.size();
        for(ResourceVersionConfiguration v: versions.values())
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

        for(ResourceVersionConfiguration v: versions.values())
        {
            if(v.getProperties().isEmpty())
            {
                count++;
            }
        }

        return count;
    }

    public Resource asResource()
    {
        Resource r = new Resource(getName());
        r.setDefaultVersion(getDefaultVersion());

        Map<String, ResourceProperty> properties = new HashMap<String, ResourceProperty>();
        for (String key : getProperties().keySet())
        {
            properties.put(key, getProperties().get(key).asResourceProperty());
        }
        r.setProperties(properties);

        Map<String, ResourceVersion> versions = new HashMap<String, ResourceVersion>();
        for (String key : getVersions().keySet())
        {
            versions.put(key, getVersions().get(key).asResourceVersion());
        }
        r.setVersions(versions);

        return r;
    }
}
