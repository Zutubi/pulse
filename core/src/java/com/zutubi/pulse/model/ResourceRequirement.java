package com.zutubi.pulse.model;

import com.zutubi.util.TextUtils;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.FieldAction;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * Identifies a required resource for a build stage.
 */
@SymbolicName("zutubi.resourceRequirementConfig")
public class ResourceRequirement extends AbstractConfiguration
{
    @Required
    @FieldAction(template = "ResourceRequirement.browser")
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
