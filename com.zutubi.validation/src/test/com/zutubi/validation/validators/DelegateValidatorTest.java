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
