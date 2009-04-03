package com.zutubi.pulse.core.commands.make;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for make.pp.
 */
public class MakePostProcessorConfigurationExamples
{
    private static final String NAME = "make.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(NAME, createEmpty());
    }

    public ConfigurationExample getApply()
    {
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(createEmpty());
    }

    private MakePostProcessorConfiguration createEmpty()
    {
        MakePostProcessorConfiguration pp = new MakePostProcessorConfiguration();
        pp.setName(NAME);
        pp.getPatterns().clear();
        return pp;
    }
}