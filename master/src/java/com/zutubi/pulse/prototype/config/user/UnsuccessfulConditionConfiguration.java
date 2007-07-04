package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.SymbolicName;

/**
 *
 *
 */
@SymbolicName("zutubi.unsuccessfulConditionConfig")
public class UnsuccessfulConditionConfiguration extends SubscriptionConditionConfiguration
{
    private long after = 5;
    
    private Units units = Units.BUILDS;

    public long getAfter()
    {
        return after;
    }

    public void setAfter(long after)
    {
        this.after = after;
    }

    public Units getUnits()
    {
        return units;
    }

    public void setUnits(Units units)
    {
        this.units = units;
    }

    public enum Units
    {
        DAYS, BUILDS
    }
}
