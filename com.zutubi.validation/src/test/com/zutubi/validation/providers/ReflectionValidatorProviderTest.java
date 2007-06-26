package com.zutubi.validation.providers;

import com.zutubi.validation.Validator;
import com.zutubi.validation.mock.MockWallet;
import com.zutubi.validation.validators.ValidateableValidator;
import junit.framework.TestCase;

import java.util.List;

/**
 * <class-comment/>
 */
public class ReflectionValidatorProviderTest extends TestCase
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
        List<Validator> validators = provider.getValidators(new MockWallet(), null);
        assertEquals(1, validators.size());

        ValidateableValidator v = (ValidateableValidator) validators.get(0);
    }

    public void testPlainOldJavaObject()
    {
        List<Validator> validators = provider.getValidators(new Object(), null);
        assertEquals(0, validators.size());
    }
}
