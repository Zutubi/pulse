package com.zutubi.pulse.prototype.config.user;

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
    private String expression;

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }
}
