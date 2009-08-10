package com.zutubi.pulse.core.engine;

import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.util.StringUtils;
import com.zutubi.validation.annotations.Required;

/**
 * A basic resource requirement configuration used when loading requirements
 * from a resources file (e.g. local builds).
 */
@SymbolicName("zutubi.simpleResourceRequirementConfig")
public class SimpleResourceRequirementConfiguration extends AbstractConfiguration
{
    @Required
    private String name;
    private String version;
    private boolean optional = false;

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

    public boolean isOptional()
    {
        return optional;
    }

    public void setOptional(boolean optional)
    {
        this.optional = optional;
    }

    public ResourceRequirement asResourceRequirement()
    {
        if (StringUtils.stringSet(version))
        {
            return new ResourceRequirement(name, version, optional);
        }
        else
        {
            return new ResourceRequirement(name, optional);
        }
    }
}
