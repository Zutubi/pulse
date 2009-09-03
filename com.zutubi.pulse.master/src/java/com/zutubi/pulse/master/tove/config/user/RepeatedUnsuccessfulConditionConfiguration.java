package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.notifications.condition.NotifyConditionFactory;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Numeric;

/**
 * A condition that becomes true after a run of unsuccessful builds for a
 * certain number of builds or days.
 */
@SymbolicName("zutubi.repeatedUnsuccessfulConditionConfig")
@Form(fieldOrder = {"after", "units"})
public class RepeatedUnsuccessfulConditionConfiguration extends SubscriptionConditionConfiguration
{
    @Numeric(min = 1)
    private int after = 5;
    private Units units = Units.BUILDS;

    public int getAfter()
    {
        return after;
    }

    public void setAfter(int after)
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
