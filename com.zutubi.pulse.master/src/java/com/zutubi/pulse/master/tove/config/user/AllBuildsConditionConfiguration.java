package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.pulse.master.condition.NotifyConditionFactory;

/**
 * A condition that is always true, and hence allows notifications for all
 * builds.
 */
@SymbolicName("zutubi.allBuildsConditionConfig")
public class AllBuildsConditionConfiguration extends SubscriptionConditionConfiguration
{
    public String getExpression()
    {
        return NotifyConditionFactory.TRUE;
    }
}
