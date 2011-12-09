package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

@Table(columns = {"resource", "displayVersion", "inverse", "optional"})
@Form(fieldOrder = {"resource", "defaultVersion", "version", "inverse", "optional"})
@SymbolicName("zutubi.resourceRequirementConfig")
public class ResourceRequirementConfiguration extends AbstractConfiguration
{
    @Required
    @FieldAction(template = "ResourceRequirementConfiguration.browser")
    private String resource;
    @ControllingCheckbox(uncheckedFields = {"version"})
    private boolean defaultVersion = true;
    @Required // if version is enabled, it is also required.
    private String version;
    @ControllingCheckbox(uncheckedFields = {"optional"})
    private boolean inverse;
    private boolean optional;

    public ResourceRequirementConfiguration()
    {
    }

    public ResourceRequirementConfiguration(String resource, String version, boolean defaultVersion, boolean optional)
    {
        this.resource = resource;
        this.version = version;
        this.defaultVersion = defaultVersion;
        this.optional = optional;
    }

    public ResourceRequirementConfiguration(ResourceRequirement requirement)
    {
        this(requirement.getResource(), requirement.getVersion(), requirement.isDefaultVersion(), requirement.isOptional());
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

    public boolean isInverse()
    {
        return inverse;
    }

    public void setInverse(boolean inverse)
    {
        this.inverse = inverse;
    }

    public boolean isOptional()
    {
        return optional;
    }

    public void setOptional(boolean optional)
    {
        this.optional = optional;
    }

    public ResourceRequirementConfiguration copy()
    {
        return new ResourceRequirementConfiguration(resource, version, defaultVersion, optional);
    }

    public String toString()
    {
        return (resource == null ? "?" : resource) + ":" + (!defaultVersion ? version : "[default]");
    }

    public ResourceRequirement asResourceRequirement()
    {
        if (isDefaultVersion())
        {
            return new ResourceRequirement(resource, inverse, optional);
        }
        else
        {
            return new ResourceRequirement(resource, version, inverse, optional);
        }
    }
}

