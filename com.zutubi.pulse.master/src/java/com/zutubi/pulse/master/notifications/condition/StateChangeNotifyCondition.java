package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.notifications.NotifyConditionContext;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

/**
 * This notify condition triggers when the received build result is the first
 * successful build after a one or more failures.
 */
public class StateChangeNotifyCondition implements NotifyCondition
{
    public StateChangeNotifyCondition()
    {
    }

    public boolean satisfied(NotifyConditionContext context, UserConfiguration user)
    {
        BuildResult buildResult = context.getBuildResult();
        if (buildResult == null)
        {
            return false;
        }

        BuildResult previous = context.getPrevious().getBuildResult();
        return previous != null && previous.getState() != buildResult.getState();
    }
}
