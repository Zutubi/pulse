package com.zutubi.pulse.master.model;

import com.zutubi.pulse.master.notifications.condition.NotifyConditionFactory;

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
