package com.cinnamonbob.xwork.validator.validators;

import com.opensymphony.xwork.ValidationAwareSupport;
import com.opensymphony.xwork.validator.DelegatingValidatorContext;
import com.opensymphony.xwork.validator.FieldValidator;
import junit.framework.TestCase;

/**
 * <class-comment/>
 */
public abstract class FieldValidatorTestBase extends TestCase
{
    protected ValidationAwareSupport validationAware;
    protected FieldValidator validator;

    public FieldValidatorTestBase(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        validationAware = new ValidationAwareSupport();
        validator = createValidator();
        validator.setFieldName("field");
        validator.setValidatorContext(new DelegatingValidatorContext(validationAware));
    }

    protected abstract FieldValidator createValidator();

    public void tearDown() throws Exception
    {
        // add tear down code here.
        validator = null;
        validationAware = null;

        super.tearDown();
    }

    protected class FieldProvider
    {
        private String value;

        public FieldProvider(String value)
        {
            this.value = value;
        }

        public String getField()
        {
            return value;
        }
    }
}