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

package com.zutubi.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;

public class PatternValidatorTest extends FieldValidatorTestCase
{
    protected FieldValidator createValidator()
    {
        return new PatternValidator();
    }

    public void testRegex() throws ValidationException
    {
        validator.validate(new FieldProvider(".*"));
        assertFalse(validationAware.hasErrors());
    }

    public void testNumeralsOnly() throws ValidationException
    {
        validator.validate(new FieldProvider("[0-9]"));
        assertFalse(validationAware.hasErrors());
    }

    public void testInvalidRegex() throws ValidationException
    {
        validator.validate(new FieldProvider("["));
        assertTrue(validationAware.hasErrors());
    }

    public void testGroupCheck() throws ValidationException
    {
        ((PatternValidator)validator).setGroupCount(2);
        validator.validate(new FieldProvider("(.+)\\.(.+)"));
        assertFalse(validationAware.hasErrors());

        validator.validate(new FieldProvider(".+\\.(.+)"));
        assertTrue(validationAware.hasErrors());
    }
}
