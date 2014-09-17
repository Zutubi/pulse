package com.zutubi.pulse.core.postprocessors.xctest;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for xctest.pp.
 */
public class XCTestReportPostProcessorConfigurationExamples
{
    private static final String NAME = "xctest.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(NAME, new XCTestReportPostProcessorConfiguration(NAME));
    }

    public ConfigurationExample getApply()
    {
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(new XCTestReportPostProcessorConfiguration(NAME));
    }
}
