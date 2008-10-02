package com.zutubi.pulse.master.model;

/**
 * A condition where the user just provides the raw expression.
 */
public class AdvancedProjectBuildCondition extends ProjectBuildCondition
{
    private String expression;

    public AdvancedProjectBuildCondition()
    {
    }

    public AdvancedProjectBuildCondition(String expression)
    {
        this.expression = expression;
    }

    public String getType()
    {
        return "advanced";
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
