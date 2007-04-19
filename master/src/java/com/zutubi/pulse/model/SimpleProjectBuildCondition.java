package com.zutubi.pulse.model;

import com.zutubi.util.StringUtils;

import java.util.List;
import java.util.Arrays;

/**
 * A condition that is a disjunction of some simple conditions.
 */
public class SimpleProjectBuildCondition extends ProjectBuildCondition
{
    private String expression;

    public SimpleProjectBuildCondition()
    {
    }

    public SimpleProjectBuildCondition(String expression)
    {
        this.expression = expression;
    }

    public SimpleProjectBuildCondition(List<String> conditions)
    {
        setConditions(conditions);
    }

    public String getType()
    {
        return "simple";
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public List<String> getConditions()
    {
        return Arrays.asList(expression.split(" or "));
    }

    public void setConditions(List<String> conditions)
    {
        expression = StringUtils.join(" or ", conditions.toArray(new String[conditions.size()]));
    }
}
