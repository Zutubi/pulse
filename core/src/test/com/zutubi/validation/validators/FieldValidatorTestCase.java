package com.zutubi.validation.validators;

import junit.framework.TestCase;
import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationAwareSupport;
import com.zutubi.validation.DelegatingValidationContext;

/**
 * <class-comment/>
 */
public abstract class FieldValidatorTestCase extends TestCase
{
    protected ValidationAwareSupport validationAware;
    protected FieldValidator validator;

    public FieldValidatorTestCase(String testName)
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
        validator.setValidationContext(new DelegatingValidationContext(validationAware));
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
        private Object value;

        public FieldProvider(Object value)
        {
            this.value = value;
        }

        public Object getField()
        {
            return value;
        }
    }
}