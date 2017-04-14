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

import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.i18n.InMemoryTextProvider;

import java.util.List;

/**
 * <class-comment/>
 */
public class FieldValidatorSupportTest extends ZutubiTestCase
{
    private InMemoryTextProvider textProvider;
    private DelegatingValidationContext validationContext;

    protected void setUp() throws Exception
    {
        super.setUp();

        textProvider = new InMemoryTextProvider();
        textProvider.addText("field.label", "field label");
        validationContext = new DelegatingValidationContext(textProvider);
    }

    protected void tearDown() throws Exception
    {
        textProvider = null;
        validationContext = null;

        super.tearDown();
    }

    public void testDefaultKey() throws ValidationException
    {
        textProvider.addText("field.invalid", "default message here");
        validateAndAssertMessage(new AddErrorFieldValidatorSupport(), "default message here");
    }

    public void testFallbackToGenericKey() throws ValidationException
    {
        textProvider.addText(".invalid", "fallback key");
        validateAndAssertMessage(new AddErrorFieldValidatorSupport(), "fallback key");
    }

    public void testDefaultArgs() throws ValidationException
    {
        textProvider.addText("field.invalid", "{0} is invalid");
        validateAndAssertMessage(new AddErrorFieldValidatorSupport(), "field label is invalid");
    }

    public void testFieldPrefixedKeyPreferred() throws ValidationException
    {
        textProvider.addText("field.invalid", "preferred");
        textProvider.addText(".invalid", "hidden");
        validateAndAssertMessage(new AddErrorFieldValidatorSupport(), "preferred");
    }

    public void testCustomDefaultKeySuffix() throws ValidationException
    {
        textProvider.addText("field.invalid", "original");
        textProvider.addText("field.mysuffix", "custom");
        FieldValidatorSupport validator = new AddErrorFieldValidatorSupport();
        validator.setDefaultKeySuffix("mysuffix");
        validateAndAssertMessage(validator, "custom");
    }

    public void testAdditionalArgs() throws ValidationException
    {
        textProvider.addText("field.invalid", "{0} arg {1}");
        FieldValidatorSupport customArg = new TestFieldValidatorSupport()
        {
            protected void validateField(Object value) throws ValidationException
            {
                addError(getDefaultKeySuffix(), "custom");
            }
        };

        validateAndAssertMessage(customArg, "field label arg custom");
    }

    public void testExplicitMessage() throws ValidationException
    {
        textProvider.addText("field.invalid", "default message");
        FieldValidatorSupport customMessage = new TestFieldValidatorSupport()
        {
            protected void validateField(Object value) throws ValidationException
            {
                addErrorMessage("custom message");
            }
        };

        validateAndAssertMessage(customMessage, "custom message");
    }

    public void testNoConfigurationMessage() throws ValidationException
    {
        validateAndAssertMessage(new AddErrorFieldValidatorSupport(), "field label is invalid");
    }

    public void testUnavailableDefaultKeySuffix() throws ValidationException
    {
        FieldValidatorSupport addError = new AddErrorFieldValidatorSupport();
        addError.setDefaultKeySuffix("unavailable");
        validateAndAssertMessage(addError, "field label is invalid");
    }

    private void validateAndAssertMessage(FieldValidatorSupport support, String message) throws ValidationException
    {
        support.validate(new Fake());
        List<String> errors = validationContext.getFieldErrors(support.getFieldName());
        assertEquals(1, errors.size());
        assertEquals(message, errors.get(0));
    }

    private abstract class TestFieldValidatorSupport extends FieldValidatorSupport
    {
        public TestFieldValidatorSupport()
        {
            setFieldName("field");
            setValidationContext(FieldValidatorSupportTest.this.validationContext);
        }
    }

    private class AddErrorFieldValidatorSupport extends TestFieldValidatorSupport
    {
        protected void validateField(Object value) throws ValidationException
        {
            addError();
        }
    }

    public static class Fake
    {
        private String field;

        public String getField()
        {
            return field;
        }

        public void setField(String field)
        {
            this.field = field;
        }
    }
}
