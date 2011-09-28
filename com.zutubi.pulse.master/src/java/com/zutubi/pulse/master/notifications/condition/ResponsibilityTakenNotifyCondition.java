package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

/**
 * This notify condition triggers when the project corresponding to the
 * received build result has a responsible user.
 */
public class ResponsibilityTakenNotifyCondition implements NotifyCondition
{
    public boolean satisfied(BuildResult result, UserConfiguration user)
    {
        return result != null && result.getProject() != null && result.getProject().getResponsibility() != null;
    }
}
