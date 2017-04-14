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

package com.zutubi.pulse.master.validation;

import com.zutubi.pulse.master.tove.config.user.CustomConditionConfiguration;
import com.zutubi.pulse.master.tove.config.user.RepeatedUnsuccessfulConditionConfiguration;
import com.zutubi.pulse.master.notifications.condition.NotifyConditionFactory;
import com.zutubi.tove.type.TypeException;
import com.zutubi.validation.ValidationException;

/**
 */
public class SubscriptionValidationTest extends AbstractValidationTestCase
{
    private NotifyConditionFactory notifyConditionFactory;

    protected void setUp() throws Exception
    {
        notifyConditionFactory = new NotifyConditionFactory();
        
        super.setUp();
    }

    public void testRepeatedUnsuccessfulConditionValid() throws TypeException, ValidationException
    {
        RepeatedUnsuccessfulConditionConfiguration condition = new RepeatedUnsuccessfulConditionConfiguration();
        condition.setAfter(10);
        validateAndAssertValid(condition);
    }

    public void testRepeatedUnsuccessfulConditionZeroAfter() throws TypeException, ValidationException
    {
        RepeatedUnsuccessfulConditionConfiguration condition = new RepeatedUnsuccessfulConditionConfiguration();
        condition.setAfter(0);

        validateAndAssertFieldErrors(condition, "after", "after.min");
    }

    public void testRepeatedUnsuccessfulConditionNegativeAfter() throws TypeException, ValidationException
    {
        RepeatedUnsuccessfulConditionConfiguration condition = new RepeatedUnsuccessfulConditionConfiguration();
        condition.setAfter(-1);

        validateAndAssertFieldErrors(condition, "after", "after.min");
    }

    public void testCustomConditionValid() throws TypeException, ValidationException
    {
        CustomConditionConfiguration condition = new CustomConditionConfiguration();
        condition.setCustomCondition("changed or not success");

        validateAndAssertValid(condition);
    }

    public void testCustomConditionInvalid() throws TypeException, ValidationException
    {
        CustomConditionConfiguration condition = new CustomConditionConfiguration();
        condition.setCustomCondition("invalid");

        validateAndAssertFieldErrors(condition, "customCondition", "line 1:1: unexpected token: invalid");
    }
}
