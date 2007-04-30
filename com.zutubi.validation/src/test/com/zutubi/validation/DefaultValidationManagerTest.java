package com.zutubi.validation;

import junit.framework.TestCase;
import com.zutubi.validation.mock.MockAnimal;
import com.zutubi.validation.mock.MockWallet;
import com.zutubi.validation.mock.MockAccount;
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
        assertEquals("animal.head.required", fieldErrors.get(0));
    }

    public void testIgnoredField() throws ValidationException
    {
        MockAnimal animal = new MockAnimal();
        validationContext.addIgnoredField("head");
        validationManager.validate(animal, validationContext);
        assertFalse(validationContext.hasErrors());
        assertFalse(validationContext.hasFieldErrors());
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

    public void testShortCircuit() throws ValidationException
    {
        // the email field is both required AND must be a valid email. Verify that if no email is specified,
        // that we do not bother with trying to ensure that it is also a valid email.

        MockAccount account = new MockAccount();
        validationManager.validate(account, validationContext);

        List<String> fieldErrors = validationContext.getFieldErrors("email");
        assertEquals(1, fieldErrors.size());
        assertEquals("email.required", fieldErrors.get(0));

        // reset the validation context.
        validationContext.setFieldErrors(null);
        assertFalse(validationContext.hasErrors());

        account.setEmail("invalid email");
        validationManager.validate(account, validationContext);

        fieldErrors = validationContext.getFieldErrors("email");
        assertEquals(1, fieldErrors.size());
        assertEquals("email.invalid", fieldErrors.get(0));
    }
}
