package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;
import com.zutubi.pulse.core.config.ResourceRequirement;

@Table(columns = {"resource", "displayVersion"})
@Form(fieldOrder = {"resource", "defaultVersion", "version"})
@SymbolicName("zutubi.resourceRequirementConfig")
public class ResourceRequirementConfiguration extends AbstractConfiguration
{
    @Required
    @FieldAction(template = "ResourceRequirement.browser")
    private String resource;

    @Required // if version is enabled, it is also required.
    private String version;

    @ControllingCheckbox(invert = true, dependentFields = {"version"})
    private boolean defaultVersion = true;

    public ResourceRequirementConfiguration()
    {
    }

    public ResourceRequirementConfiguration(String resource, String version, boolean defaultVersion)
    {
        this.resource = resource;
        this.version = version;
        this.defaultVersion = defaultVersion;
    }

    public String getResource()
    {
        return resource;
    }

    public void setResource(String resource)
    {
        this.resource = resource;
    }

    public boolean isDefaultVersion()
    {
        return defaultVersion;
    }

    public void setDefaultVersion(boolean defaultVersion)
    {
        this.defaultVersion = defaultVersion;
    }

    public String getVersion()
    {
        return version;
    }

    @Transient
    public String getDisplayVersion()
    {
        if (isDefaultVersion())
        {
            return "[default]";
        }
        return getVersion();
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public ResourceRequirementConfiguration copy()
    {
        return new ResourceRequirementConfiguration(resource, version, defaultVersion);
    }

    public String toString()
    {
        return (resource == null ? "?" : resource) + ":" + (!defaultVersion ? version : "[default]");
    }

    public ResourceRequirement asResourceRequirement()
    {
        return new ResourceRequirement(getResource(), getVersion(), isDefaultVersion());
    }
}

