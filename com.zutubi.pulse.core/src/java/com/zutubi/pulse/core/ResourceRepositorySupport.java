package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.config.ResourceRequirement;

/**
 * Support base class for implementing resource repositories.  Implements
 * convenience methods on top of the basic resource access method.
 */
public abstract class ResourceRepositorySupport implements ResourceRepository
{
    public boolean hasResource(ResourceRequirement requirement)
    {
        ResourceConfiguration resource = getResource(requirement.getResource());
        return resource != null && hasRequiredVersion(resource, requirement);
    }

    private boolean hasRequiredVersion(ResourceConfiguration resource, ResourceRequirement requirement)
    {
        return requirement.isDefaultVersion() || resource.getVersion(requirement.getVersion()) != null;
    }

    public boolean hasResource(String name)
    {
        return getResource(name) != null;
    }
}
