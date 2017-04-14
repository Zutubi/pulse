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

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateRangeValidatorTest extends FieldValidatorTestCase
{
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    protected FieldValidator createValidator()
    {
        return new DateRangeValidator();
    }

    public void setUp() throws Exception
    {
        super.setUp();

        ((DateRangeValidator)validator).setMin("01/01/1990");
        ((DateRangeValidator)validator).setMax("01/01/2010");
    }

    public void testValidRange() throws ValidationException, ParseException
    {
        validator.validate(new FieldProvider(dateFormat.parse("01/01/2000")));
        assertFalse(validationAware.hasErrors());
    }

    public void testBelowRange() throws ValidationException, ParseException
    {
        validator.validate(new FieldProvider(dateFormat.parse("01/01/1980")));
        assertTrue(validationAware.hasErrors());
    }

    public void testAboveRange() throws ValidationException, ParseException
    {
        validator.validate(new FieldProvider(dateFormat.parse("01/01/2020")));
        assertTrue(validationAware.hasErrors());
    }

    public void testUpperRangeBoundry() throws ValidationException, ParseException
    {
        validator.validate(new FieldProvider(dateFormat.parse("01/01/2010")));
        assertFalse(validationAware.hasErrors());
    }

    public void testLowerRangeBoundry() throws ValidationException, ParseException
    {
        validator.validate(new FieldProvider(dateFormat.parse("01/01/1990")));
        assertFalse(validationAware.hasErrors());
    }

}
