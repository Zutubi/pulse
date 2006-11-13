package com.zutubi.pulse.model;

/**
 * Identifies a required resource for a build stage.
 */
public class ResourceRequirement
{
    private String resource;
    private String version;

    public ResourceRequirement()
    {
    }

    public ResourceRequirement(String resource, String version)
    {
        this.resource = resource;
        this.version = version;
    }

    public String getResource()
    {
        return resource;
    }

    public void setResource(String resource)
    {
        this.resource = resource;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public ResourceRequirement copy()
    {
        return new ResourceRequirement(resource, version);
    }
}
