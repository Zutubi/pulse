/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
