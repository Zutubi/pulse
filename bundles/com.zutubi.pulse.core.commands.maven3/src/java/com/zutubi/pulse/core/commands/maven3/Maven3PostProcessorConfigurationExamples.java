package com.zutubi.pulse.core.commands.maven3;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for maven3.pp.
 */
public class Maven3PostProcessorConfigurationExamples
{
    private static final String NAME = "maven3.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(NAME, new Maven3PostProcessorConfiguration(NAME));
    }

    public ConfigurationExample getApply()
    {
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(new Maven3PostProcessorConfiguration(NAME));
    }
}