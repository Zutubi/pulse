package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceRequirement;

import java.util.List;

/**
 */
public interface ResourceRepository
{
    boolean hasResource(ResourceRequirement requirement);

    boolean hasResource(String name);

    Resource getResource(String name);

    List<String> getResourceNames();
}
