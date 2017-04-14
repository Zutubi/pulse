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

import com.zutubi.validation.DefaultValidationManager;
import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.providers.AnnotationValidatorProvider;
import com.zutubi.validation.providers.ReflectionValidatorProvider;
import com.zutubi.validation.types.TestDoor;

import java.util.Arrays;

public class DelegateValidatorTest extends FieldValidatorTestCase
{
    private DefaultValidationManager validationManager;

    protected FieldValidator createValidator()
    {
        return new DelegateValidator();
    }

    public void setUp() throws Exception
    {
        super.setUp();

        validationManager = new DefaultValidationManager();
        validationManager.addValidatorProvider(new AnnotationValidatorProvider());
        validationManager.addValidatorProvider(new ReflectionValidatorProvider());
        ((DelegateValidator)validator).setValidationManager(validationManager);
    }

    public void tearDown() throws Exception
    {
        validationManager = null;

        super.tearDown();
    }

    public void testDelegationToSingleObject() throws ValidationException
    {
        validator.validate(new FieldProvider(new TestDoor()));

        assertTrue(validationAware.hasFieldErrors());
        assertEquals(Arrays.asList("handle.required"), validationAware.getFieldErrors("field.handle"));
    }

    public void testDelegationToCollection() throws ValidationException
    {
        validator.validate(new FieldProvider(Arrays.asList(new TestDoor(), new TestDoor(), new TestDoor())));

        assertTrue(validationAware.hasFieldErrors());
        assertEquals(Arrays.asList("handle.required"), validationAware.getFieldErrors("field[0].handle"));
        assertEquals(Arrays.asList("handle.required"), validationAware.getFieldErrors("field[1].handle"));
        assertEquals(Arrays.asList("handle.required"), validationAware.getFieldErrors("field[2].handle"));
    }
}
