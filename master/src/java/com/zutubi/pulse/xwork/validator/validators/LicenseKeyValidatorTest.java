package com.zutubi.pulse.xwork.validator.validators;

import com.opensymphony.xwork.validator.FieldValidator;
import com.zutubi.pulse.test.LicenseHelper;

import java.util.Arrays;

/**
 * <class-comment/>
 */
public class LicenseKeyValidatorTest extends FieldValidatorTestBase
{
    public LicenseKeyValidatorTest(String testName)
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
        LicenseKeyValidator licenseKeyValidator = new LicenseKeyValidator();
        licenseKeyValidator.setMessageKey("license.key.invalid");
        return licenseKeyValidator;
    }

    public void testNull() throws Exception
    {
        validator.validate(new FieldProvider(null));
        assertTrue(validationAware.hasErrors());
        assertEquals(Arrays.asList("license.key.invalid"), validationAware.getFieldErrors().get("field"));
    }

    public void testEmptyString() throws Exception
    {
        validator.validate(new FieldProvider(""));
        assertTrue(validationAware.hasErrors());
        assertEquals(Arrays.asList("license.key.invalid"), validationAware.getFieldErrors().get("field"));
    }

    public void testInvalidLicenseString() throws Exception
    {
        String key = LicenseHelper.newLicenseKey("dummy", "dummy", null);
        validator.validate(new FieldProvider(key.substring(2)));
        assertTrue(validationAware.hasErrors());
        assertEquals(Arrays.asList("license.key.invalid"), validationAware.getFieldErrors().get("field"));
    }

    public void testValidLicenseString() throws Exception
    {
        String key = LicenseHelper.newLicenseKey("dummy", "dummy", null);
        validator.validate(new FieldProvider(key));
        assertFalse(validationAware.hasErrors());
    }
}
