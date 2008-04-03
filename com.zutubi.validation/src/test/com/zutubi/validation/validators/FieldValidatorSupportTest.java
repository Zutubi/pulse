package com.zutubi.validation.validators;

import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.i18n.InMemoryTextProvider;
import junit.framework.TestCase;

import java.util.List;

/**
 * <class-comment/>
 */
public class FieldValidatorSupportTest extends TestCase
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
        support.validate(new Mock());
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

    public static class Mock
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
