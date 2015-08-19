package com.zutubi.validation.providers;

import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.validation.Validator;
import com.zutubi.validation.types.TestWallet;
import com.zutubi.validation.validators.ValidateableValidator;

import java.util.List;

/**
 * <class-comment/>
 */
public class ReflectionValidatorProviderTest extends ZutubiTestCase
{
    private ReflectionValidatorProvider provider;

    protected void setUp() throws Exception
    {
        super.setUp();

        provider = new ReflectionValidatorProvider();
    }

    protected void tearDown() throws Exception
    {
        provider = null;

        super.tearDown();
    }

    public void testValidateableObject()
    {
        List<Validator> validators = provider.getValidators(TestWallet.class, null);
        assertEquals(1, validators.size());

        assertTrue(validators.get(0) instanceof ValidateableValidator);
    }

    public void testPlainOldJavaObject()
    {
        List<Validator> validators = provider.getValidators(Object.class, null);
        assertEquals(0, validators.size());
    }
}
