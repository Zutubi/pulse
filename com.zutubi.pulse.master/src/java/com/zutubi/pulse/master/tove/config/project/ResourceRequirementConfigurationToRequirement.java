package com.zutubi.pulse.master.tove.config.project;

import com.google.common.base.Function;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.tove.variables.api.VariableMap;

/**
 * Maps from resource requirement configurations to resource requirements.
 */
public class ResourceRequirementConfigurationToRequirement implements Function<ResourceRequirementConfiguration, ResourceRequirement>
{
    private VariableMap variables;

    public ResourceRequirementConfigurationToRequirement(VariableMap variables)
    {
        this.variables = variables;
    }

    public ResourceRequirement apply(ResourceRequirementConfiguration resourceRequirementConfiguration)
    {
        return resourceRequirementConfiguration.asResourceRequirement(variables);
    }
}
