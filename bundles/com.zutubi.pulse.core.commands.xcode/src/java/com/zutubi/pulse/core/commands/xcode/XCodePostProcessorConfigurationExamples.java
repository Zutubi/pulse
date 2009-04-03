package com.zutubi.pulse.core.commands.xcode;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for xcodebuild.pp.
 */
public class XCodePostProcessorConfigurationExamples
{
    private static final String NAME = "xcode.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(NAME, createEmpty());
    }

    public ConfigurationExample getApply()
    {
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(createEmpty());
    }

    private XCodePostProcessorConfiguration createEmpty()
    {
        XCodePostProcessorConfiguration pp = new XCodePostProcessorConfiguration();
        pp.setName(NAME);
        pp.getPatterns().clear();
        return pp;
    }
}