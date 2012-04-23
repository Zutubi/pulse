package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.notifications.NotifyConditionContext;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

/**
 * This notify condition triggers when the project corresponding to the
 * received build result has a responsible user.
 */
public class ResponsibilityTakenNotifyCondition implements NotifyCondition
{
    public boolean satisfied(NotifyConditionContext context, UserConfiguration user)
    {
        BuildResult buildResult = context.getBuildResult();
        return buildResult != null && buildResult.getProject() != null && buildResult.getProject().getResponsibility() != null;
    }
}
