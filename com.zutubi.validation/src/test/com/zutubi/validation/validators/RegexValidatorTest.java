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

public class RegexValidatorTest extends FieldValidatorTestCase
{
    protected FieldValidator createValidator()
    {
        return new RegexValidator();
    }

    public void testRegex() throws ValidationException
    {
        ((RegexValidator)validator).setPattern(".*");
        validator.validate(new FieldProvider("blah"));
        assertFalse(validationAware.hasErrors());
    }

    public void testNumeralsOnly() throws ValidationException
    {
        ((RegexValidator)validator).setPattern("[0-9]");
        validator.validate(new FieldProvider("blah"));
        assertTrue(validationAware.hasErrors());
    }
}
