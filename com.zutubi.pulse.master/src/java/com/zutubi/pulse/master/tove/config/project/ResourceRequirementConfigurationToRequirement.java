package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.util.Mapping;

/**
 * Maps from resource requirement configurations to resource requirements.
 */
public class ResourceRequirementConfigurationToRequirement implements Mapping<ResourceRequirementConfiguration, ResourceRequirement>
{
    public ResourceRequirement map(ResourceRequirementConfiguration resourceRequirementConfiguration)
    {
        return resourceRequirementConfiguration.asResourceRequirement();
    }
}
