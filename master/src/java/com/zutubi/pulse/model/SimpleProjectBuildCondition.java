package com.zutubi.pulse.model;

import com.zutubi.pulse.util.StringUtils;

import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;

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
        if (expression != null && expression.length() > 0)
        {
            return Arrays.asList(expression.split(" or "));
        }
        return new LinkedList<String>();
    }

    public void setConditions(List<String> conditions)
    {
        if (conditions != null && conditions.size() > 0)
        {
            expression = StringUtils.join(" or ", conditions.toArray(new String[conditions.size()]));
        }
    }
}
