package com.zutubi.pulse.core.scm.cvs.validation.validators;

import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.validators.FieldValidatorTestCase;
import junit.framework.Assert;

public class CvsRootValidatorTest extends FieldValidatorTestCase
{
    protected FieldValidator createValidator()
    {
        return new CvsRootValidator();
    }

    public void testEmptyString() throws Exception
    {
        validator.validate(new FieldProvider(""));
        Assert.assertTrue(validationAware.hasErrors());
    }

    public void testNull() throws Exception
    {
        validator.validate(new FieldProvider(null));
        Assert.assertFalse(validationAware.hasErrors());
    }

    public void testLocalRoot() throws Exception
    {
        validator.validate(new FieldProvider("/local"));
        Assert.assertFalse(validationAware.hasErrors());
    }

    public void testPSever() throws Exception
    {
        validator.validate(new FieldProvider(":pserver:blah@somehost.com:/path/to/root"));
        Assert.assertFalse(validationAware.hasErrors());
    }
}