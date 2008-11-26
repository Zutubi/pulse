package com.zutubi.pulse.core;

import com.zutubi.pulse.core.postprocessors.api.Feature;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 *
 *
 */
public class RegexPattern
{
    private Feature.Level category = Feature.Level.ERROR;
    private String expression;
    private String summary;
    private Pattern pattern;
    private List<ExpressionElement> exclusions;

    public RegexPattern()
    {
        exclusions = new LinkedList<ExpressionElement>();
    }

    public RegexPattern(Feature.Level category, Pattern pattern)
    {
        this.category = category;
        this.pattern = pattern;
        this.expression = pattern.pattern();
        exclusions = new LinkedList<ExpressionElement>();
    }

    public Feature.Level getCategory()
    {
        return category;
    }

    public void setCategory(Feature.Level level)
    {
        this.category = level;
    }

    public void setCategory(String category) throws FileLoadException
    {
        try
        {
            this.category = Feature.Level.valueOf(category.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            throw new FileLoadException("Unrecognised regex category '" + category + "'");
        }
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression) throws FileLoadException
    {
        try
        {
            pattern = Pattern.compile(expression);
            this.expression = expression;
        }
        catch (PatternSyntaxException e)
        {
            throw new FileLoadException(e);
        }
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public Pattern getPattern()
    {
        return pattern;
    }

    public void setPattern(Pattern pattern)
    {
        this.pattern = pattern;
    }

    public ExpressionElement createExclude()
    {
        ExpressionElement exclusion = new ExpressionElement();
        exclusions.add(exclusion);
        return exclusion;
    }

    public List<ExpressionElement> getExclusions()
    {
        return exclusions;
    }

    public String match(String line)
    {
        String result = null;

        Matcher matcher = pattern.matcher(line);
        if (matcher.find())
        {
            for (ExpressionElement e : exclusions)
            {
                if (e.getPattern().matcher(line).find())
                {
                    return null;
                }
            }

            if (summary == null)
            {
                result = line;
            }
            else
            {
                result = matcher.replaceAll(summary);
            }
        }

        return result;
    }

    public void addExclusion(Pattern pattern)
    {
        exclusions.add(new ExpressionElement(pattern));
    }

    public void addExclusion(ExpressionElement element)
    {
        exclusions.add(element);
    }
}
