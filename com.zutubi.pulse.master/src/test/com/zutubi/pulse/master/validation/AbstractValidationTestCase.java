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

import static com.google.common.collect.Lists.newArrayList;
import com.zutubi.tove.config.AbstractConfigurationSystemTestCase;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.validation.FakeValidationContext;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;

import java.util.List;

/**
 * A base for validation test cases, allowing convenient testing of the
 * validation rules for configuration types.
 */
public abstract class AbstractValidationTestCase extends AbstractConfigurationSystemTestCase
{
    protected void assertErrors(List<String> gotErrors, String... expectedErrors)
    {
        assertEquals(expectedErrors.length, gotErrors.size());
        for(int i = 0; i < expectedErrors.length; i++)
        {
            assertEquals(expectedErrors[i], gotErrors.get(i));
        }
    }

    protected void validateAndAssertValid(Configuration instance) throws ValidationException
    {
        ValidationContext context = new FakeValidationContext();
        validationManager.validate(instance, context);
        assertFalse(context.hasErrors());
    }

    protected void validateAndAssertFieldErrors(Configuration instance, String field, String... expectedErrors) throws ValidationException
    {
        ValidationContext context = new FakeValidationContext();
        validationManager.validate(instance, context);
        assertTrue(context.hasErrors());
        assertErrors(context.getFieldErrors(field), expectedErrors);
    }

    protected void validateAndAssertInstanceErrors(Configuration instance, String... expectedErrors) throws ValidationException
    {
        ValidationContext context = new FakeValidationContext();
        validationManager.validate(instance, context);
        assertTrue(context.hasErrors());
        assertErrors(newArrayList(context.getActionErrors()), expectedErrors);
    }
}
