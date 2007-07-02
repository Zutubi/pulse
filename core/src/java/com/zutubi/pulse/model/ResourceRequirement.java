package com.zutubi.pulse.model;

import com.opensymphony.util.TextUtils;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;

/**
 * Identifies a required resource for a build stage.
 */
@SymbolicName("internal.resourceRequirementConfig")
public class ResourceRequirement extends AbstractConfiguration
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

    public String toString()
    {
        return (resource == null ? "?" : resource) + ":" + (TextUtils.stringSet(version) ? version : "[default]");
    }
}
