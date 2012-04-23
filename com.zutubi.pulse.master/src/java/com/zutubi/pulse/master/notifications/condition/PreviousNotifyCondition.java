package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.master.notifications.NotifyConditionContext;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

/**
 * A notify condition that applies a delegate condition to the previous build result.
 */
public class PreviousNotifyCondition implements NotifyCondition
{
    private NotifyCondition delegate;

    public PreviousNotifyCondition(NotifyCondition delegate)
    {
        this.delegate = delegate;
    }

    public boolean satisfied(NotifyConditionContext context, UserConfiguration user)
    {
        return delegate.satisfied(context.getPrevious(), user);
    }

    public NotifyCondition getDelegate()
    {
        return delegate;
    }
}
