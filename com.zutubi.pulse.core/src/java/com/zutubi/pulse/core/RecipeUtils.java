package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.util.TextUtils;

import java.util.List;

public class RecipeUtils
{
    public static void addResourceProperties(ExecutionContext context, List<ResourceRequirement> resourceRequirements, ResourceRepository resourceRepository)
    {
        if (resourceRequirements != null)
        {
            for (ResourceRequirement requirement: resourceRequirements)
            {
                Resource resource = resourceRepository.getResource(requirement.getResource());
                if (resource != null)
                {
                    for (ResourceProperty property: resource.getProperties().values())
                    {
                        context.add(property);
                    }

                    String importVersion = requirement.getVersion();
                    if (requirement.isDefaultVersion())
                    {
                        importVersion = resource.getDefaultVersion();
                    }

                    if (TextUtils.stringSet(importVersion))
                    {
                        ResourceVersion version = resource.getVersion(importVersion);
                        if (version != null)
                        {
                            for(ResourceProperty property: version.getProperties().values())
                            {
                                context.add(property);
                            }
                        }
                    }
                }
            }
        }
    }
}
