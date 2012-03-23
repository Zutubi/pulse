package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.tove.variables.api.VariableMap;
import com.zutubi.util.Mapping;

/**
 * Maps from resource requirement configurations to resource requirements.
 */
public class ResourceRequirementConfigurationToRequirement implements Mapping<ResourceRequirementConfiguration, ResourceRequirement>
{
    private VariableMap variables;

    public ResourceRequirementConfigurationToRequirement(VariableMap variables)
    {
        this.variables = variables;
    }

    public ResourceRequirement map(ResourceRequirementConfiguration resourceRequirementConfiguration)
    {
        return resourceRequirementConfiguration.asResourceRequirement(variables);
    }
}
