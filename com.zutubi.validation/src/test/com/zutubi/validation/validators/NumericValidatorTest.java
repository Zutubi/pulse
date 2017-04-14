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

public class NumericValidatorTest extends FieldValidatorTestCase
{
    public void setUp() throws Exception
    {
        super.setUp();
        
        textProvider.addText("field.min", "field.min");
        textProvider.addText("field.max", "field.max");
        textProvider.addText("field.invalid", "field.invalid");
    }

    protected FieldValidator createValidator()
    {
        return new NumericValidator();
    }

    public void testStringIsNumeric() throws ValidationException
    {
        assertValidation(Long.MIN_VALUE, Long.MAX_VALUE, "1");
        assertValidation(Long.MIN_VALUE, Long.MAX_VALUE, "a", "field.invalid");
    }

    public void testMinMaxValidation() throws ValidationException
    {
        assertValidation(5, 10, 7);
    }

    public void testMinInclusiveValidation() throws ValidationException
    {
        assertValidation(5, 5, 5);
    }

    public void testMinValidationInt() throws ValidationException
    {
        assertValidation(5, Long.MAX_VALUE, 3, "field.min");
    }

    public void testMaxValidationInt() throws ValidationException
    {
        assertValidation(Long.MIN_VALUE, 2, 3, "field.max");
    }

    public void testMinValidationShort() throws ValidationException
    {
        assertValidation(5, Long.MAX_VALUE, (short)3, "field.min");
    }

    public void testMaxValidationShort() throws ValidationException
    {
        assertValidation(Long.MIN_VALUE, 2, (short)3, "field.max");
    }

    public void testMinValidationByte() throws ValidationException
    {
        assertValidation(5, Long.MAX_VALUE, (byte)3, "field.min");
    }

    public void testMaxValidationByte() throws ValidationException
    {
        assertValidation(Long.MIN_VALUE, 2, (byte)3, "field.max");
    }

    public void testMinValidationLong() throws ValidationException
    {
        assertValidation(5, Long.MAX_VALUE, (long)3, "field.min");
    }

    public void testMaxValidationLong() throws ValidationException
    {
        assertValidation(Long.MIN_VALUE, 2, (long)3, "field.max");
    }

    public void testUnsetInt() throws ValidationException
    {
        assertValidation(0, Long.MAX_VALUE, Integer.MIN_VALUE);
    }

    public void testUnsetNull() throws ValidationException
    {
        assertValidation(0, Long.MAX_VALUE, null);
    }

    public void testUnsetLong() throws ValidationException
    {
        assertValidation(0, Long.MAX_VALUE, Long.MIN_VALUE);
    }

    private void assertValidation(long min, long max, Object value, String... errors) throws ValidationException
    {
        if (min != Long.MIN_VALUE)
        {
            ((NumericValidator)validator).setMin(min);
        }
        if (max != Long.MAX_VALUE)
        {
            ((NumericValidator)validator).setMax(max);
        }

        validator.validate(new FieldProvider(value));

        assertEquals(errors.length > 0, validationAware.hasFieldErrors());
        if (errors.length > 0)
        {
            assertEquals(Arrays.asList(errors), validationAware.getFieldErrors("field"));
        }
        else
        {
            assertNull(validationAware.getFieldErrors("field"));
        }
    }
}
