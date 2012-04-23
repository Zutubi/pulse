package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.master.notifications.NotifyConditionContext;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

public class TrueNotifyCondition implements NotifyCondition
{
    public boolean satisfied(NotifyConditionContext context, UserConfiguration user)
    {
        return true;
    }

}