package com.zutubi.pulse.core.config;

/**
 * Identifies a required resource for a build stage.
 */
public class ResourceRequirement
{
    private String resource;

    private String version;

    private boolean defaultVersion = true;

    public ResourceRequirement()
    {
    }

    public ResourceRequirement(String resource, String version, boolean defaultVersion)
    {
        this.resource = resource;
        this.version = version;
        this.defaultVersion = defaultVersion;
    }

    public String getResource()
    {
        return resource;
    }

    public boolean isDefaultVersion()
    {
        return defaultVersion;
    }

    public String getVersion()
    {
        return version;
    }

    public ResourceRequirement copy()
    {
        return new ResourceRequirement(resource, version, defaultVersion);
    }

    public String toString()
    {
        return (resource == null ? "?" : resource) + ":" + (!defaultVersion ? version : "[default]");
    }
}
