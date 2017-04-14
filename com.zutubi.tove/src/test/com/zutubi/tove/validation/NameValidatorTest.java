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

package com.zutubi.tove.validation;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorTestCase;

public class NameValidatorTest extends FieldValidatorTestCase
{
    protected FieldValidator createValidator()
    {
        return new NameValidator();
    }

    public void testDisallowTrailingSpace() throws ValidationException
    {
        validator.validate(new FieldProvider("blah "));
        assertTrue(validationAware.hasFieldErrors());
    }

    public void testDisallowTrailingTab() throws ValidationException
    {
        validator.validate(new FieldProvider("blah\t"));
        assertTrue(validationAware.hasFieldErrors());
    }

    public void testDisallowLeadingSpace() throws ValidationException
    {
        validator.validate(new FieldProvider(" blah"));
        assertTrue(validationAware.hasFieldErrors());
    }

    public void testDisallowLeadingTab() throws ValidationException
    {
        validator.validate(new FieldProvider("\tblah"));
        assertTrue(validationAware.hasFieldErrors());
    }

    public void testDisallowForwardSlash() throws ValidationException
    {
        validator.validate(new FieldProvider("take a /"));
        assertTrue(validationAware.hasFieldErrors());
    }

    public void testDisallowBackwardSlash() throws ValidationException
    {
        validator.validate(new FieldProvider("take a \\"));
        assertTrue(validationAware.hasFieldErrors());
    }

    public void testDisallowDollar() throws ValidationException
    {
        validator.validate(new FieldProvider("take the $ and run"));
        assertTrue(validationAware.hasFieldErrors());
    }

    public void testSingleCharacterName() throws ValidationException
    {
        validator.validate(new FieldProvider("b"));
        assertFalse(validationAware.hasFieldErrors());
    }

    public void testMultipleCharacterWord() throws ValidationException
    {
        validator.validate(new FieldProvider("blah"));
        assertFalse(validationAware.hasFieldErrors());
    }

    public void testUnderscoreSeparated() throws ValidationException
    {
        validator.validate(new FieldProvider("a_a"));
        assertFalse(validationAware.hasFieldErrors());
    }

    public void testBeginsWithUnderscore() throws ValidationException
    {
        validator.validate(new FieldProvider("_a"));
        assertFalse(validationAware.hasFieldErrors());
    }

    public void testSingleUnderscore() throws ValidationException
    {
        validator.validate(new FieldProvider("_"));
        assertFalse(validationAware.hasFieldErrors());
    }

    public void testAllowMultipleWords() throws ValidationException
    {
        validator.validate(new FieldProvider("blah blah blah"));
        assertFalse(validationAware.hasFieldErrors());
    }

    public void testAllowMultipleSingleCharacterWords() throws ValidationException
    {
        validator.validate(new FieldProvider("b b b"));
        assertFalse(validationAware.hasFieldErrors());
    }
}
