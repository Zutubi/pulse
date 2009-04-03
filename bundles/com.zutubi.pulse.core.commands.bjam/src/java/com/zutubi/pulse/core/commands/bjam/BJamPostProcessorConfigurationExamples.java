package com.zutubi.pulse.core.commands.bjam;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example ant post-processors.
 */
public class BJamPostProcessorConfigurationExamples
{
    private static final String NAME = "bjam.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(NAME, createEmpty());
    }

    public ConfigurationExample getApply()
    {
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(createEmpty());
    }

    private BJamPostProcessorConfiguration createEmpty()
    {
        BJamPostProcessorConfiguration pp = new BJamPostProcessorConfiguration(NAME);
        pp.getPatterns().clear();
        return pp;
    }
}