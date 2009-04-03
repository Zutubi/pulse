package com.zutubi.pulse.core.commands.core;

import com.zutubi.tove.config.api.ConfigurationExample;

/**
 * Example configurations for post-processor.
 */
public class PostProcessorGroupConfigurationExamples
{
    private static final String NAME = "build.pp";

    public ConfigurationExample getApply()
    {
        RegexPostProcessorConfiguration compilePP = new RegexPostProcessorConfiguration("compile.pp");
        compilePP.addErrorRegexes("^.+:[0-9]+: error:");

        RegexTestPostProcessorConfiguration testPP = new RegexTestPostProcessorConfiguration("test.pp");

        PostProcessorGroupConfiguration pp = new PostProcessorGroupConfiguration();
        pp.setName(NAME);
        pp.addPostProcessor(compilePP);
        pp.addPostProcessor(testPP);

        return ExamplesBuilder.buildProjectForCommandOutputProcessor(pp);
    }
}