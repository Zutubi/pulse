package com.zutubi.pulse.core.commands.core;

import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for junit-summary.pp.
 */
public class JUnitSummaryPostProcessorConfigurationExamples
{
    private static final String NAME = "junit-summary.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(NAME, createEmpty());
    }

    public ConfigurationExample getApply()
    {
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(createEmpty());
    }

    private JUnitSummaryPostProcessorConfiguration createEmpty()
    {
        JUnitSummaryPostProcessorConfiguration pp = new JUnitSummaryPostProcessorConfiguration();
        pp.setName(NAME);
        pp.getPatterns().clear();
        return pp;
    }
}