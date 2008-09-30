package com.zutubi.pulse.model;

import com.zutubi.config.annotations.ControllingCheckbox;
import com.zutubi.config.annotations.FieldAction;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Table;
import com.zutubi.config.annotations.Wizard;
import com.zutubi.config.annotations.Transient;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * Identifies a required resource for a build stage.
 */
@Table(columns = {"resource", "displayVersion"})
@Form(fieldOrder = {"resource", "defaultVersion", "version"})
@SymbolicName("zutubi.resourceRequirementConfig")
public class ResourceRequirement extends AbstractConfiguration
{
    @Required
    @FieldAction(template = "ResourceRequirement.browser")
    private String resource;

    @Required // if version is enabled, it is also required.
    private String version;

    @ControllingCheckbox(invert = true, dependentFields = {"version"})
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

    public ResourceRequirement copy()
    {
        return new ResourceRequirement(resource, version, defaultVersion);
    }

    public String toString()
    {
        return (resource == null ? "?" : resource) + ":" + (!defaultVersion ? version : "[default]");
    }
}
