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
            return NotifyConditionFactory.BROKEN_COUNT_BUILDS + " == " + after;
        }
        else
        {
            return NotifyConditionFactory.BROKEN_COUNT_DAYS + "(previous) < " + after + " and " + NotifyConditionFactory.BROKEN_COUNT_DAYS + " >= " + after;
        }
    }

    public enum Units
    {
        DAYS, BUILDS
    }
}
