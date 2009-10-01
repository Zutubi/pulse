package com.zutubi.validation.validators;

import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationAwareSupport;
import com.zutubi.validation.i18n.InMemoryTextProvider;

public abstract class FieldValidatorTestCase extends ZutubiTestCase
{
    protected ValidationAwareSupport validationAware;
    protected InMemoryTextProvider textProvider;
    protected FieldValidator validator;

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        validationAware = new ValidationAwareSupport();
        textProvider = new InMemoryTextProvider();

        validator = createValidator();
        validator.setFieldName("field");
        validator.setValidationContext(new DelegatingValidationContext(validationAware, textProvider));
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