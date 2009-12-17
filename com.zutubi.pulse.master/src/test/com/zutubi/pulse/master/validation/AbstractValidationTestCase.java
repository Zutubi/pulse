package com.zutubi.pulse.master.validation;

import com.zutubi.tove.config.AbstractConfigurationSystemTestCase;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.validation.FakeValidationContext;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;

import java.util.List;

/**
 * A base for validation test cases, allowing conventient testing of the
 * validation rules for configuration types.
 */
public abstract class AbstractValidationTestCase extends AbstractConfigurationSystemTestCase
{
    protected void assertErrors(List<String> gotErrors, String... expectedErrors)
    {
        assertEquals(expectedErrors.length, gotErrors.size());
        for(int i = 0; i < expectedErrors.length; i++)
        {
            assertEquals(expectedErrors[i], gotErrors.get(i));
        }
    }

    protected void validateAndAssertValid(Configuration instance) throws ValidationException
    {
        ValidationContext context = new FakeValidationContext();
        validationManager.validate(instance, context);
        assertFalse(context.hasErrors());
    }

    protected void validateAndAssertFieldErrors(Configuration instance, String field, String... expectedErrors) throws ValidationException
    {
        ValidationContext context = new FakeValidationContext();
        validationManager.validate(instance, context);
        assertTrue(context.hasErrors());
        assertErrors(context.getFieldErrors(field), expectedErrors);
    }

    protected void validateAndAssertInstanceErrors(Configuration instance, String... expectedErrors) throws ValidationException
    {
        ValidationContext context = new FakeValidationContext();
        validationManager.validate(instance, context);
        assertTrue(context.hasErrors());
        assertErrors(CollectionUtils.asList(context.getActionErrors()), expectedErrors);
    }
}
