package com.zutubi.pulse.core.postprocessors.clang;

import com.zutubi.pulse.core.commands.core.ExamplesBuilder;
import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for clang.pp.
 */
public class ClangPostProcessorConfigurationExamples
{
    private static final String NAME = "clang.pp";

    public ConfigurationExample getTrivial()
    {
        return new ConfigurationExample(NAME, createEmpty());
    }

    public ConfigurationExample getApply()
    {
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(createEmpty());
    }

    private ClangPostProcessorConfiguration createEmpty()
    {
        ClangPostProcessorConfiguration pp = new ClangPostProcessorConfiguration();
        pp.setName(NAME);
        pp.getPatterns().clear();
        return pp;
    }
}
