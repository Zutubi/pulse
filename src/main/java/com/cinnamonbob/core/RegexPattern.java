package com.cinnamonbob.core;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.cinnamonbob.model.Feature;

/**
 * 
 *
 */
public class RegexPattern
{
    private Feature.Level category;
    private String expression;

    private Pattern pattern;

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
        catch(IllegalArgumentException e)
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
        catch(PatternSyntaxException e)
        {
            throw new FileLoadException(e);
        }
    }

    public Pattern getPattern()
    {
        return pattern;
    }

}
