package com.zutubi.pulse.core.commands.msbuild;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for msbuild.pp.
 */
public class MsBuildPostProcessorConfigurationExamples
{
    private static final String NAME = "msbuild.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(NAME, createEmpty());
    }

    public ConfigurationExample getApply()
    {
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(createEmpty());
    }

    private MsBuildPostProcessorConfiguration createEmpty()
    {
        MsBuildPostProcessorConfiguration pp = new MsBuildPostProcessorConfiguration(NAME);
        pp.getPatterns().clear();
        return pp;
    }
}