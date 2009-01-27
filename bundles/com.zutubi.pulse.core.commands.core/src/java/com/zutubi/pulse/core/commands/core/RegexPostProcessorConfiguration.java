package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.RegexPatternConfiguration;
import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.pulse.core.postprocessors.api.LineBasedPostProcessorConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * A post processor that does line-by-line searching with regular expressions
 * to detect features.
 */
@SymbolicName("zutubi.regexPostProcessorConfig")
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

    public void addErrorRegexs(String... errorRegexs)
    {
        addRegexes(Feature.Level.ERROR, errorRegexs);
    }

    public void addWarningRegexs(String... warningRegexs)
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