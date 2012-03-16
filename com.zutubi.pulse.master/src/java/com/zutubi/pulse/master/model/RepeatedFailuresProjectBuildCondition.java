package com.zutubi.pulse.master.model;

import com.zutubi.pulse.master.notifications.condition.NotifyConditionFactory;

/**
 * A condition that fires after repeated build failures.
 */
public class RepeatedFailuresProjectBuildCondition extends ProjectBuildCondition
{
    public static final String UNITS_BUILDS = "builds";
    public static final String UNITS_DAYS = "days";

    private int x;
    private String units;

    public RepeatedFailuresProjectBuildCondition()
    {
    }

    public RepeatedFailuresProjectBuildCondition(int x, String units)
    {
        this.x = x;
        this.units = units;
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public String getUnits()
    {
        return units;
    }

    public void setUnits(String units)
    {
        this.units = units;
    }

    public String getType()
    {
        return "repeated";
    }

    public String getExpression()
    {
        if(UNITS_BUILDS.equals(units))
        {
            return NotifyConditionFactory.BROKEN_COUNT_BUILDS + " == " + x;
        }
        else
        {
            return NotifyConditionFactory.BROKEN_COUNT_DAYS + "(previous) < " + x + " and " + NotifyConditionFactory.BROKEN_COUNT_DAYS + " >= " + x;
        }
    }
}
