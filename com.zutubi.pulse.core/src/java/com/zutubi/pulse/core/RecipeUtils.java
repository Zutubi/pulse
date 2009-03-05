package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.config.ResourceVersionConfiguration;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.util.TextUtils;

import java.util.List;

public class RecipeUtils
{
    public static void addResourceProperties(ExecutionContext context, List<ResourceRequirement> resourceRequirements, ResourceRepository resourceRepository)
    {
        if (resourceRequirements != null)
        {
            for(ResourceRequirement requirement: resourceRequirements)
            {
                ResourceConfiguration resource = resourceRepository.getResource(requirement.getResource());
                if(resource == null)
                {
                    return;
                }

                for(ResourcePropertyConfiguration property: resource.getProperties().values())
                {
                    context.add(property.asResourceProperty());
                }

                String importVersion = requirement.getVersion();
                if(requirement.isDefaultVersion())
                {
                    importVersion = resource.getDefaultVersion();
                }

                if(TextUtils.stringSet(importVersion))
                {
                    ResourceVersionConfiguration version = resource.getVersion(importVersion);
                    if(version == null)
                    {
                        return;
                    }

                    for(ResourcePropertyConfiguration property: version.getProperties().values())
                    {
                        context.add(property.asResourceProperty());
                    }
                }
            }
        }
    }
}
