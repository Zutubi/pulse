package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.servercore.validation.annotation.SubscriptionCondition;

/**
 * A project build subscription condition configured with a custom boolean
 * expression.
 */
@SymbolicName("zutubi.customConditionConfig")
public class CustomConditionConfiguration extends SubscriptionConditionConfiguration
{
    @SubscriptionCondition
    private String customCondition;

    // Note this is a transient property, so we do not use the same name for
    // the actual expression property.
    public String getExpression()
    {
        return customCondition;
    }

    public String getCustomCondition()
    {
        return customCondition;
    }

    public void setCustomCondition(String customCondition)
    {
        this.customCondition = customCondition;
    }
}
