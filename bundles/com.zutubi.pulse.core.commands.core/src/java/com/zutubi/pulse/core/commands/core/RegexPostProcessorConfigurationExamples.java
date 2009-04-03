package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.RegexPatternConfiguration;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.tove.config.api.ConfigurationExample;

import java.util.regex.Pattern;

/**
 * Example configurations for regex.pp.
 */
public class RegexPostProcessorConfigurationExamples
{
    public ConfigurationExample getCompiler()
    {
        RegexPostProcessorConfiguration pp = new RegexPostProcessorConfiguration("compile.pp");
        pp.addErrorRegexes("^.+:[0-9]+: error:");
        pp.addWarningRegexes("^.+:[0-9]+: warning:");
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(pp);
    }

    public ConfigurationExample getExclusions()
    {
        RegexPostProcessorConfiguration pp = new RegexPostProcessorConfiguration("errors.pp");
        pp.setFailOnError(false);
        pp.setLeadingContext(3);
        pp.setTrailingContext(5);
        RegexPatternConfiguration pattern = new RegexPatternConfiguration(Feature.Level.ERROR, Pattern.compile("[Ee]rror"));
        pattern.addExclusion("MyError.java");
        pattern.addExclusion("terror.txt");
        return ExamplesBuilder.buildProjectForCommandOutputProcessor(pp);
    }
}