package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.notifications.NotifyConditionContext;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

/**
 * Simple notify condition for terminated builds.
 */
public class TerminatedNotifyCondition implements NotifyCondition
{
    public boolean satisfied(NotifyConditionContext context, UserConfiguration user)
    {
        BuildResult buildResult = context.getBuildResult();
        return buildResult != null && buildResult.terminated();
    }
}
