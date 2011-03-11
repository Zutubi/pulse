package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceVersionConfiguration;
import com.zutubi.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class RecipeUtils
{
    public static final String SUPPRESSED_VALUE = "[value suppressed for security reasons]";
    
    private static final String PROPERTY_SUPPRESSED_ENVIRONMENT = "pulse.suppressed.environment.variables";

    public static List<String> getSuppressedEnvironment()
    {
        return Arrays.asList(System.getProperty(PROPERTY_SUPPRESSED_ENVIRONMENT, "P4PASSWD PULSE_TEST_SUPPRESSED").split(" +"));
    }

    public static void addResourceProperties(ExecutionContext context, List<ResourceRequirement> resourceRequirements, ResourceRepository resourceRepository)
    {
        if (resourceRequirements != null)
        {
            for (ResourceRequirement requirement : resourceRequirements)
            {
                ResourceConfiguration resource = resourceRepository.getResource(requirement.getResource());
                if (resource != null)
                {
                    for (ResourcePropertyConfiguration property : resource.getProperties().values())
                    {
                        context.add(property.asResourceProperty());
                    }

                    String importVersion = requirement.getVersion();
                    if (requirement.isDefaultVersion())
                    {
                        importVersion = resource.getDefaultVersion();
                    }

                    if (StringUtils.stringSet(importVersion))
                    {
                        ResourceVersionConfiguration version = resource.getVersion(importVersion);
                        if (version != null)
                        {
                            for (ResourcePropertyConfiguration property : version.getProperties().values())
                            {
                                context.add(property.asResourceProperty());
                            }
                        }
                    }
                }
            }
        }
    }
}
