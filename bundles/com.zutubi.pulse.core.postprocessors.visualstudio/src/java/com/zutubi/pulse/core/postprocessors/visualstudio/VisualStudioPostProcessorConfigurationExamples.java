package com.zutubi.pulse.core.postprocessors.visualstudio;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for visualstudio.pp.
 */
public class VisualStudioPostProcessorConfigurationExamples
{
    private static final String NAME = "visualstudio.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(NAME, createEmpty());
    }

    public ConfigurationExample getApply()
    {
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(createEmpty());
    }

    private VisualStudioPostProcessorConfiguration createEmpty()
    {
        VisualStudioPostProcessorConfiguration pp = new VisualStudioPostProcessorConfiguration();
        pp.setName(NAME);
        pp.getPatterns().clear();
        return pp;
    }
}
