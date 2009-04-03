package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for maven2.pp.
 */
public class Maven2PostProcessorConfigurationExamples
{
    private static final String NAME = "maven2.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(NAME, new Maven2PostProcessorConfiguration(NAME));
    }

    public ConfigurationExample getApply()
    {
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(new Maven2PostProcessorConfiguration(NAME));
    }
}