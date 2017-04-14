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

package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorTestCase;

public class IvyPatternValidatorTest extends FieldValidatorTestCase
{
    protected FieldValidator createValidator()
    {
        return new IvyPatternValidator();
    }

    public void testValidPattern() throws Exception
    {
        validator.validate(new FieldProvider("[artifact]"));
        assertFalse(validationAware.hasErrors());
    }

    public void testIncompletePattern() throws ValidationException
    {
        validator.validate(new FieldProvider("[artifact"));
        assertTrue(validationAware.hasErrors());
    }

    public void testInvalidPattern() throws ValidationException
    {
        validator.validate(new FieldProvider("[[artifact]"));
        assertTrue(validationAware.hasErrors());
    }

    public void testPulsePropertyReference() throws ValidationException
    {
        validator.validate(new FieldProvider("${something}"));
        assertFalse(validationAware.hasErrors());
    }

    public void testPulsePropertyReferenceAndPattern() throws ValidationException
    {
        validator.validate(new FieldProvider("[${something}]"));
        assertFalse(validationAware.hasErrors());
        validator.validate(new FieldProvider("${s[omethi]ng}"));
        assertFalse(validationAware.hasErrors());
    }

}
