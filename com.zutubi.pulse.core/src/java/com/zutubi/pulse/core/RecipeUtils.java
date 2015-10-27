package com.zutubi.pulse.core;

import com.google.common.collect.Sets;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceVersionConfiguration;
import com.zutubi.tove.variables.api.Variable;
import com.zutubi.util.StringUtils;
import com.zutubi.util.SystemUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecipeUtils
{
    public static final String SUPPRESSED_VALUE = "[value suppressed for security reasons]";
    
    private static final String PROPERTY_SUPPRESSED_ENVIRONMENT = "pulse.suppressed.environment.variables";

    public static Set<String> getSuppressedEnvironment()
    {
        return Sets.newHashSet(System.getProperty(PROPERTY_SUPPRESSED_ENVIRONMENT, "P4PASSWD PULSE_TEST_SUPPRESSED").split(" +"));
    }

    public static void addPulseEnvironment(PulseExecutionContext context, ProcessBuilder builder)
    {
        PulseScope scope = context.getScope();
        Map<String, String> childEnvironment = builder.environment();
        Set<String> suppressedEnvironment = getSuppressedEnvironment();
        for (Variable variable : scope.getVariables(String.class))
        {
            if (acceptableName(variable.getName(), suppressedEnvironment))
            {
                childEnvironment.put(convertName(variable.getName()), (String) variable.getValue());
            }
        }
    }

    /**
     * Is the specified name an acceptable name for adding to the child processes environment.
     * If it is already in the environment (env. prefix), then we return false.
     *
     * @param name variable name to check
     * @param suppressedEnvironment a set of names that are suppressed for security reasons
     *
     * @return return false if the name contains the 'env.' prefix, or contains an unsupported
     * character
     */
    static boolean acceptableName(String name, Set<String> suppressedEnvironment)
    {
        if (name.startsWith("env."))
        {
            return false;
        }

        if (suppressedEnvironment.contains(name.toUpperCase()))
        {
            return false;
        }

        if (SystemUtils.IS_WINDOWS)
        {
            return name.matches("[-a-zA-Z._0-9<>|&^% ]+");
        }

        return name.matches("[-a-zA-Z._0-9]+");
    }

    static String convertName(String name)
    {
        name = name.toUpperCase();
        name = name.replaceAll("\\.", "_");

        return "PULSE_" + name;
    }

    public static void addResourceProperties(ExecutionContext context, List<ResourceRequirement> resourceRequirements, ResourceRepository resourceRepository)
    {
        if (resourceRequirements != null)
        {
            for (ResourceRequirement requirement : resourceRequirements)
            {
                if (!requirement.isInverse())
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
}
