package com.cinnamonbob.core;

import com.cinnamonbob.core.model.Feature;

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
    private Feature.Level category;
    private String expression;
    private String summary;
    private Pattern pattern;
    private List<Exclusion> exclusions;

    public RegexPattern()
    {
        exclusions = new LinkedList<Exclusion>();
    }

    public RegexPattern(Feature.Level category, Pattern pattern)
    {
        this.category = category;
        this.pattern = pattern;
        this.expression = pattern.pattern();
        exclusions = new LinkedList<Exclusion>();
    }

    public Feature.Level getCategory()
    {
        return category;
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

    public void setCategory(Feature.Level category)
    {
        this.category = category;
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

    public Exclusion createExclude()
    {
        Exclusion exclusion = new Exclusion();
        exclusions.add(exclusion);
        return exclusion;
    }

    public String match(String line)
    {
        String result = null;

        Matcher matcher = pattern.matcher(line);
        if (matcher.find())
        {
            for (Exclusion e : exclusions)
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
        exclusions.add(new Exclusion(pattern));
    }

    class Exclusion
    {
        private String expression;
        private Pattern pattern;

        public Exclusion()
        {

        }

        public Exclusion(Pattern pattern)
        {
            this.pattern = pattern;
            expression = pattern.pattern();
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

        public Pattern getPattern()
        {
            return pattern;
        }
    }
}
