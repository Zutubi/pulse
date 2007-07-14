package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;

/**
 * A condition that needs to be satisfied before a project build subscription
 * will result in a notification.
 */
@SymbolicName("zutubi.subscriptionConditionConfig")
public abstract class SubscriptionConditionConfiguration extends AbstractConfiguration
{
    public abstract String getExpression();
}
