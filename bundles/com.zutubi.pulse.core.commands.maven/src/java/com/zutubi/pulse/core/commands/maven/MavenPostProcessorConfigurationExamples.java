package com.zutubi.pulse.core.commands.maven;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for maven.pp.
 */
public class MavenPostProcessorConfigurationExamples
{
    private static final String NAME = "maven.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(NAME, new MavenPostProcessorConfiguration(NAME));
    }

    public ConfigurationExample getApply()
    {
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(new MavenPostProcessorConfiguration(NAME));
    }
}