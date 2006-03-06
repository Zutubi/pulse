package com.cinnamonbob.xwork.validator.validators;

import com.opensymphony.xwork.validator.FieldValidator;

/**
 * <class-comment/>
 */
public class CvsRootValidatorTest extends FieldValidatorTestBase
{
    public CvsRootValidatorTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    protected FieldValidator createValidator()
    {
        return new CvsRootValidator();
    }

    public void testEmptyString() throws Exception
    {
        validator.validate(new FieldProvider(""));
        assertTrue(validationAware.hasErrors());
    }

    public void testNull() throws Exception
    {
        validator.validate(new FieldProvider(null));
        assertTrue(validationAware.hasErrors());
    }

    public void testLocalRoot() throws Exception
    {
        validator.validate(new FieldProvider("/local"));
        assertFalse(validationAware.hasErrors());
    }

    public void testPSever() throws Exception
    {
        validator.validate(new FieldProvider(":pserver:blah@somehost.com:/path/to/root"));
        assertFalse(validationAware.hasErrors());
    }
}