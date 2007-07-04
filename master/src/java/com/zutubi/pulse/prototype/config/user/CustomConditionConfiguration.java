package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.SymbolicName;

/**
 *
 *
 */
@SymbolicName("zutubi.customConditionConfig")
public class CustomConditionConfiguration extends SubscriptionConditionConfiguration
{
    //TODO: add custom validation for this expression
    private String expression;
}
