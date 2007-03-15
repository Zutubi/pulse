package com.zutubi.pulse.core;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A simple element to hold an expression that is used in multiple places.
 */
public class ExpressionElement
{
    private String expression;
    private Pattern pattern;

    public ExpressionElement()
    {

    }

    public ExpressionElement(Pattern pattern)
    {
        this.pattern = pattern;
        expression = pattern.pattern();
    }

    public String getExpression()
    {
        return expression;
    }

    public void setPattern(Pattern pattern)
    {
        this.pattern = pattern;
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
