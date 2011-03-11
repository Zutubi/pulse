package com.zutubi.pulse.core.resources;

import com.zutubi.util.StringUtils;

/**
 * Identifies a required resource for a build stage.
 */
public class ResourceRequirement
{
    private String resource;
    private String version;
    private boolean optional;

    public ResourceRequirement(String resource, boolean optional)
    {
        this(resource, null, optional);
    }

    public ResourceRequirement(String resource, String version, boolean optional)
    {
        this.resource = resource;
        this.version = version;
        this.optional = optional;
    }

    public String getResource()
    {
        return resource;
    }

    public boolean isOptional()
    {
        return optional;
    }
    
    public boolean isDefaultVersion()
    {
        return !StringUtils.stringSet(version);
    }

    public String getVersion()
    {
        return version;
    }

    public ResourceRequirement copy()
    {
        return new ResourceRequirement(resource, version, optional);
    }

    public String toString()
    {
        return (resource == null ? "?" : resource) + ":" + (!isDefaultVersion() ? version : "[default]");
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ResourceRequirement that = (ResourceRequirement) o;

        if (optional != that.optional)
        {
            return false;
        }
        if (resource != null ? !resource.equals(that.resource) : that.resource != null)
        {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = resource != null ? resource.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (optional ? 1 : 0);
        return result;
    }
}
