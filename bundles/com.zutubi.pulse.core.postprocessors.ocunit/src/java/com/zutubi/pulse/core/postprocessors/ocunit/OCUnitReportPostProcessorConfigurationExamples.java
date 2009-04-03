package com.zutubi.pulse.core.postprocessors.ocunit;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for ocunit.pp.
 */
public class OCUnitReportPostProcessorConfigurationExamples
{
    private static final String NAME = "ocunit.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(NAME, new OCUnitReportPostProcessorConfiguration(NAME));
    }

    public ConfigurationExample getApply()
    {
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(new OCUnitReportPostProcessorConfiguration(NAME));
    }
}
