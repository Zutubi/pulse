package com.cinnamonbob.core.config;

import java.util.regex.Pattern;

/**
 * 
 *
 */
public class RegexPattern
{
    private String category;
    private String expression;

    private Pattern pattern;

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public Pattern getPattern()
    {
        if (pattern == null && expression != null)
        {
            pattern = Pattern.compile(expression);
        }
        return pattern;
    }

}
