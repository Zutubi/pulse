package com.cinnamonbob.xwork.validator.validators;

import com.opensymphony.xwork.ValidationAwareSupport;
import com.opensymphony.xwork.validator.DelegatingValidatorContext;
import com.opensymphony.xwork.validator.FieldValidator;
import junit.framework.TestCase;

/**
 * <class-comment/>
 */
public class CvsRootValidatorTest extends TestCase
{
    private ValidationAwareSupport validationAware;
    private FieldValidator validator;

    public CvsRootValidatorTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        validationAware = new ValidationAwareSupport();
        validator = new CvsRootValidator();
        validator.setFieldName("cvsRoot");
        validator.setValidatorContext(new DelegatingValidatorContext(validationAware));
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.

        super.tearDown();
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

    private class FieldProvider
    {
        private String value;

        public FieldProvider(String value)
        {
            this.value = value;
        }

        public String getCvsRoot()
        {
            return value;
        }
    }
}