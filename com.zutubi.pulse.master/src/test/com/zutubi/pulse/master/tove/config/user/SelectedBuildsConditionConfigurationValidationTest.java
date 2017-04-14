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

import com.zutubi.pulse.master.validation.AbstractValidationTestCase;
import com.zutubi.tove.type.TypeException;
import com.zutubi.validation.ValidationException;

/**
 */
public class SelectedBuildsConditionConfigurationValidationTest extends AbstractValidationTestCase
{
    public void testConditionsSelected() throws TypeException, ValidationException
    {
        SelectedBuildsConditionConfiguration condition = new SelectedBuildsConditionConfiguration();
        condition.setIncludeChanges(true);
        validateAndAssertValid(condition);
    }

    public void testNoConditionsSelected() throws TypeException, ValidationException
    {
        validateAndAssertInstanceErrors(new SelectedBuildsConditionConfiguration(), "please select at least one condition");
    }
}
