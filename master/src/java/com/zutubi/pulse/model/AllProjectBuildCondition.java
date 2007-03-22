package com.zutubi.pulse.model;

import com.zutubi.pulse.condition.NotifyConditionFactory;

/**
 * A condition that matches all builds.
 */
public class AllProjectBuildCondition extends ProjectBuildCondition
{
    public String getType()
    {
        return "all";
    }

    public String getExpression()
    {
        return NotifyConditionFactory.TRUE;
    }
}
