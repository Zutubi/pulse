package com.zutubi.validation;

import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.validation.providers.AnnotationValidatorProvider;
import com.zutubi.validation.providers.ReflectionValidatorProvider;
import com.zutubi.validation.types.TestAccount;
import com.zutubi.validation.types.TestAnimal;
import com.zutubi.validation.types.TestWallet;

import java.util.Arrays;
import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultValidationManagerTest extends ZutubiTestCase
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
        TestAnimal animal = new TestAnimal();
        validationManager.validate(animal, validationContext);
        assertTrue(validationContext.hasErrors());
        assertTrue(validationContext.hasFieldErrors());

        List<String> fieldErrors = validationContext.getFieldErrors("head");
        assertNotNull(fieldErrors);
        assertEquals(1, fieldErrors.size());
        assertEquals("head.myrequired", fieldErrors.get(0));
    }

    public void testIgnoredField() throws ValidationException
    {
        TestAnimal animal = new TestAnimal();
        validationContext.addIgnoredField("head");
        validationManager.validate(animal, validationContext);
        assertFalse(validationContext.hasErrors());
        assertFalse(validationContext.hasFieldErrors());
    }

    public void testMockWallet() throws ValidationException
    {
        TestWallet wallet = new TestWallet();
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

        TestAccount account = new TestAccount();
        validationManager.validate(account, validationContext);

        List<String> fieldErrors = validationContext.getFieldErrors("email");
        assertEquals(1, fieldErrors.size());
        assertEquals("email.required", fieldErrors.get(0));

        // reset the validation context.
        validationContext.clearFieldErrors();
        assertFalse(validationContext.hasErrors());

        account.setEmail("invalid email");
        validationManager.validate(account, validationContext);

        fieldErrors = validationContext.getFieldErrors("email");
        assertEquals(1, fieldErrors.size());
        assertEquals("email.invalid", fieldErrors.get(0));
    }
}
