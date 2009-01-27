package com.zutubi.pulse.core;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.ValidRegex;

/**
 * A simple element to hold an expression that is used in multiple places.
 */
@SymbolicName("zutubi.expressionElementConfiguration")
public class ExpressionElementConfiguration extends AbstractConfiguration
{
    @ValidRegex
    private String expression;

    public ExpressionElementConfiguration()
    {

    }

    public ExpressionElementConfiguration(String expression)
    {
        this.expression = expression;
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }
}
