package com.zutubi.pulse.core.config;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A resource is something that is required by a build.  It may be an
 * external tool, a certain operating system, or some virtual concept.
 */
@Form(fieldOrder = { "name", "defaultVersion" })
@Table(columns = {"name", "defaultVersion"})
@SymbolicName("zutubi.resource")
public class ResourceConfiguration  extends AbstractNamedConfiguration
{
    @Ordered @Addable("property")
    private Map<String, ResourcePropertyConfiguration> properties = new LinkedHashMap<String, ResourcePropertyConfiguration>();

    @Select(optionProvider = "com.zutubi.pulse.master.tove.config.core.ResourceVersionOptionProvider")
    private String defaultVersion;
    @Addable("version")
    private Map<String, ResourceVersionConfiguration> versions = new HashMap<String, ResourceVersionConfiguration>();

    public ResourceConfiguration()
    {

    }

    public ResourceConfiguration(String name)
    {
        setName(name);
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
}
