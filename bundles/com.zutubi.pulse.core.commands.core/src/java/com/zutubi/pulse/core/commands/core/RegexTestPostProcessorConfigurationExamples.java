package com.zutubi.pulse.core.commands.core;

import com.zutubi.tove.config.api.ConfigurationExample;

import static java.util.Arrays.asList;

/**
 * Example configurations for regex-test.pp.
 */
public class RegexTestPostProcessorConfigurationExamples
{
    public ConfigurationExample getSimple()
    {
        RegexTestPostProcessorConfiguration pp = new RegexTestPostProcessorConfiguration("sample-regex-test");
        pp.setStatusGroup(1);
        pp.setNameGroup(2);
        pp.setPassStatus(asList("PASS"));
        pp.setFailureStatus(asList("FAIL"));
        pp.setRegex("(.*) - .* - (.*)");
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(pp);
    }
}
