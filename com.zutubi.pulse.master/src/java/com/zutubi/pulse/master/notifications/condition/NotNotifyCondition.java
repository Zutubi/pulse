package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.master.notifications.NotifyConditionContext;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

public class NotNotifyCondition implements NotifyCondition
{
    private NotifyCondition condition;

    public NotNotifyCondition(NotifyCondition condition)
    {
        this.condition = condition;
    }

    public boolean satisfied(NotifyConditionContext context, UserConfiguration user)
    {
        return !condition.satisfied(context, user);
    }

    public NotifyCondition getCondition()
    {
        return condition;
    }
}