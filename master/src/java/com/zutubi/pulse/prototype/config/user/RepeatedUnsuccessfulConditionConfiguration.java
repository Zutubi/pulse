package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.condition.NotifyConditionFactory;

/**
 * A condition that becomes true after a run of unsuccessful builds for a
 * certain number of builds or days.
 */
@SymbolicName("zutubi.repeatedUnsuccessfulConditionConfig")
public class RepeatedUnsuccessfulConditionConfiguration extends SubscriptionConditionConfiguration
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

    public String getExpression()
    {
        if(units == Units.BUILDS)
        {
            return NotifyConditionFactory.UNSUCCESSFUL_COUNT_BUILDS + " == " + after;
        }
        else
        {
            return NotifyConditionFactory.UNSUCCESSFUL_COUNT_DAYS + "(previous) < " + after + " and " + NotifyConditionFactory.UNSUCCESSFUL_COUNT_DAYS + " >= " + after;
        }
    }

    public enum Units
    {
        DAYS, BUILDS
    }
}
