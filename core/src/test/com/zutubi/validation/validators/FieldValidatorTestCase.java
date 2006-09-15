package com.zutubi.validation.validators;

import junit.framework.TestCase;
import com.zutubi.validation.FieldValidator;
import com.zutubi.validation.ValidationAwareSupport;
import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.i18n.InMemoryTextProvider;
import com.zutubi.validation.i18n.LocaleProvider;
import com.zutubi.validation.i18n.DefaultLocaleProvider;

/**
 * <class-comment/>
 */
public abstract class FieldValidatorTestCase extends TestCase
{
    protected ValidationAwareSupport validationAware;
    protected InMemoryTextProvider textProvider;
    protected LocaleProvider localeProvider;
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
        textProvider = new InMemoryTextProvider();
        localeProvider = new DefaultLocaleProvider();

        validator = createValidator();
        validator.setFieldName("field");
        validator.setValidationContext(new DelegatingValidationContext(validationAware, localeProvider, textProvider));
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