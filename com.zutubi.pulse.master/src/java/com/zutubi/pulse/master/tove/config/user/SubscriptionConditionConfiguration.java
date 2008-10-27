package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.AbstractConfiguration;

/**
 * A condition that needs to be satisfied before a project build subscription
 * will result in a notification.
 */
@SymbolicName("zutubi.subscriptionConditionConfig")
public abstract class SubscriptionConditionConfiguration extends AbstractConfiguration
{
    @Transient
    public abstract String getExpression();
}
