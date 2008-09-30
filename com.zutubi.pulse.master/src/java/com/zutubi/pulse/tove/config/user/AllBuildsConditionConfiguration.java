package com.zutubi.pulse.tove.config.user;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.condition.NotifyConditionFactory;

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
