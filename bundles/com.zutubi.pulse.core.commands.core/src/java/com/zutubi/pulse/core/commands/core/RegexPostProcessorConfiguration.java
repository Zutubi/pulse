package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.RegexPatternConfiguration;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.pulse.core.postprocessors.api.LineBasedPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Configuration for instances of {@link com.zutubi.pulse.core.commands.core.RegexPostProcessor}.
 */
@SymbolicName("zutubi.regexPostProcessorConfig")
@Form(fieldOrder = {"name", "failOnError", "failOnWarning", "leadingContext", "trailingContext", "joinOverlapping"})
public class RegexPostProcessorConfiguration extends LineBasedPostProcessorConfigurationSupport
{
    @Addable("pattern")
    private List<RegexPatternConfiguration> patterns = new LinkedList<RegexPatternConfiguration>();

    public RegexPostProcessorConfiguration()
    {
        this(RegexPostProcessor.class);
    }

    public RegexPostProcessorConfiguration(String name)
    {
        this(RegexPostProcessor.class, name);
    }

    public RegexPostProcessorConfiguration(Class<? extends RegexPostProcessor> postProcessorType)
    {
        super(postProcessorType);
    }

    public RegexPostProcessorConfiguration(Class<? extends RegexPostProcessor> postProcessorType, String name)
    {
        super(postProcessorType);
        setName(name);
    }

    public List<RegexPatternConfiguration> getPatterns()
    {
        return patterns;
    }

    public void setPatterns(List<RegexPatternConfiguration> patterns)
    {
        this.patterns = patterns;
    }

    public void addErrorRegexes(String... errorRegexs)
    {
        addRegexes(Feature.Level.ERROR, errorRegexs);
    }

    public void addWarningRegexes(String... warningRegexs)
    {
        addRegexes(Feature.Level.WARNING, warningRegexs);
    }

    public void addRegexes(Feature.Level level, String... regexes)
    {
        for (String errorRegex : regexes)
        {
            patterns.add(new RegexPatternConfiguration(level, Pattern.compile(errorRegex)));
        }
    }
}