package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.util.TextUtils;
import com.zutubi.validation.annotations.Required;

/**
 * Simplified resource requirement configuration for local build resource
 * files.
 */
public class SimpleResourceRequirement
{
    @Required
    private String name;
    private String version;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public ResourceRequirement asResourceRequirement()
    {
        return new ResourceRequirement(name, version, !TextUtils.stringSet(version));
    }
}
