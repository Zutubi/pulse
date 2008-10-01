package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.RegexPattern;
import com.zutubi.pulse.core.model.Feature;
import com.zutubi.pulse.core.postprocessors.LineBasedPostProcessorSupport;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * A post processor that does line-by-line searching with regular expressions
 * to detect features.
 */
public class RegexPostProcessor extends LineBasedPostProcessorSupport
{
    private List<RegexPattern> patterns = new LinkedList<RegexPattern>();

    public RegexPostProcessor()
    {
    }

    public RegexPostProcessor(String name)
    {
        setName(name);
    }

    protected List<Feature> findFeatures(String line)
    {
        List<Feature> features = new LinkedList<Feature>();
        for (RegexPattern p : patterns)
        {
            String summary = p.match(line);
            if (summary != null)
            {
                features.add(new Feature(p.getCategory(), summary));
            }
        }

        return features;
    }

    public RegexPattern createPattern()
    {
        RegexPattern pattern = new RegexPattern();
        addRegexPattern(pattern);
        return pattern;
    }

    /* Hrm, if we call this addPattern it gets magically picked up by FileLoader */
    public void addRegexPattern(RegexPattern pattern)
    {
        patterns.add(pattern);
    }

    public List<RegexPattern> getPatterns()
    {
        return patterns;
    }

    protected void addErrorRegexs(String... errorRegexs)
    {
        for (String errorRegex : errorRegexs)
        {
            RegexPattern pattern = createPattern();
            pattern.setPattern(Pattern.compile(errorRegex));
            pattern.setCategory(Feature.Level.ERROR);
        }
    }

    protected void addWarningRegexs(String... warningRegexs)
    {
        for (String warningRegex : warningRegexs)
        {
            RegexPattern pattern = createPattern();
            pattern.setPattern(Pattern.compile(warningRegex));
            pattern.setCategory(Feature.Level.WARNING);
        }
    }
}
