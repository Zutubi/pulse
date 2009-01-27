package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.ValidRegex;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 *
 */
@SymbolicName("zutubi.regexPatternConfig")
public class RegexPatternConfiguration extends AbstractConfiguration
{
    private Feature.Level category = Feature.Level.ERROR;
    @ValidRegex
    private String expression;
    private String summary;
    @Addable("exclude")
    private List<ExpressionElementConfiguration> exclusions = new LinkedList<ExpressionElementConfiguration>();

    public RegexPatternConfiguration()
    {
        exclusions = new LinkedList<ExpressionElementConfiguration>();
    }

    public RegexPatternConfiguration(Feature.Level category, Pattern pattern)
    {
        this.category = category;
        this.expression = pattern.pattern();
        exclusions = new LinkedList<ExpressionElementConfiguration>();
    }

    public Feature.Level getCategory()
    {
        return category;
    }

    public void setCategory(Feature.Level level)
    {
        this.category = level;
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public List<ExpressionElementConfiguration> getExclusions()
    {
        return exclusions;
    }

    public void setExclusions(List<ExpressionElementConfiguration> exclusions)
    {
        this.exclusions = exclusions;
    }
}
