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

import java.util.Arrays;

public class RequiredValidatorTest extends FieldValidatorTestCase
{
    protected FieldValidator createValidator()
    {
        return new RequiredValidator();
    }

    public void setUp() throws Exception
    {
        super.setUp();

        textProvider.addText("field.required", "field.required");
    }

    public void testNullObject() throws ValidationException
    {
        validator.validate(new FieldProvider(null));
        assertTrue(validationAware.hasFieldErrors());
        assertEquals(Arrays.asList("field.required"), validationAware.getFieldErrors("field"));
    }

    public void testEmptyString() throws ValidationException
    {
        validator.validate(new FieldProvider(""));
        assertTrue(validationAware.hasFieldErrors());

        assertEquals(Arrays.asList("field.required"), validationAware.getFieldErrors("field"));
    }

    public void testObject() throws ValidationException
    {
        validator.validate(new FieldProvider(new Object()));
        assertFalse(validationAware.hasErrors());
    }

    public void testSomeString() throws ValidationException
    {
        validator.validate(new FieldProvider("asdfasf"));
        assertFalse(validationAware.hasErrors());
    }

    public void testEmptyCollection() throws ValidationException
    {
        validator.validate(new FieldProvider(Arrays.asList()));
        assertTrue(validationAware.hasErrors());
    }

    public void testNonEmptyCollection() throws ValidationException
    {
        validator.validate(new FieldProvider(Arrays.asList("blah")));
        assertFalse(validationAware.hasErrors());
    }

    public void testDefaultErrorMessageKey() throws ValidationException
    {
        textProvider.addText("field.required", "Required Field");
        validator.validate(new FieldProvider(""));
        assertTrue(validationAware.hasFieldErrors());
        assertEquals(Arrays.asList("Required Field"), validationAware.getFieldErrors("field"));
    }

    public void testErrorMessage() throws ValidationException
    {
        textProvider.addText(".required", "Required Field");
        textProvider.addText("field.required", "Field is Required");
        validator.validate(new FieldProvider(""));
        assertTrue(validationAware.hasFieldErrors());
        assertEquals(Arrays.asList("Field is Required"), validationAware.getFieldErrors("field"));
    }
}
