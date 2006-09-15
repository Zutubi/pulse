package com.zutubi.validation;

import junit.framework.TestCase;
import com.zutubi.validation.mock.MockAnimal;
import com.zutubi.validation.mock.MockWallet;
import com.zutubi.validation.providers.AnnotationValidatorProvider;
import com.zutubi.validation.providers.ReflectionValidatorProvider;

import java.util.List;
import java.util.Arrays;

/**
 * <class-comment/>
 */
public class DefaultValidationManagerTest extends TestCase
{
    private DefaultValidationManager validationManager;
    private ValidationContext validationContext;

    protected void setUp() throws Exception
    {
        super.setUp();

        validationManager = new DefaultValidationManager();
        validationManager.addValidatorProvider(new AnnotationValidatorProvider());
        validationManager.addValidatorProvider(new ReflectionValidatorProvider());
        validationContext = new DelegatingValidationContext(this);
    }

    protected void tearDown() throws Exception
    {
        validationManager = null;
        validationContext = null;

        super.tearDown();
    }

    public void testMockAnimal() throws ValidationException
    {
        MockAnimal animal = new MockAnimal();
        validationManager.validate(animal, validationContext);
        assertTrue(validationContext.hasErrors());
        assertTrue(validationContext.hasFieldErrors());

        List<String> fieldErrors = validationContext.getFieldErrors("head");
        assertNotNull(fieldErrors);
        assertEquals(1, fieldErrors.size());
        assertEquals("head.required", fieldErrors.get(0));
    }

    public void testMockWallet() throws ValidationException
    {
        MockWallet wallet = new MockWallet();
        validationManager.validate(wallet, validationContext);

        assertTrue(validationContext.hasErrors());
        assertTrue(validationContext.hasFieldErrors());

        List<String> ccErrors = validationContext.getFieldErrors("cc");
        assertEquals(Arrays.asList("cc.required"), ccErrors);

        List<String> moneyErrors = validationContext.getFieldErrors("money");
        assertEquals(Arrays.asList("money.min"), moneyErrors);
    }
}
