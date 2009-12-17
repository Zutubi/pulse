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
        List<Validator> validators = provider.getValidators(new TestWallet(), null);
        assertEquals(1, validators.size());

        ValidateableValidator v = (ValidateableValidator) validators.get(0);
    }

    public void testPlainOldJavaObject()
    {
        List<Validator> validators = provider.getValidators(new Object(), null);
        assertEquals(0, validators.size());
    }
}
