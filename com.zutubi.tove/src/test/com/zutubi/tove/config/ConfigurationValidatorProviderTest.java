package com.zutubi.tove.config;

import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.validation.EssentialValidator;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.junit.ZutubiTestCase;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.Validator;
import com.zutubi.validation.ValidatorProvider;
import com.zutubi.validation.i18n.MessagesTextProvider;
import com.zutubi.validation.validators.RegexValidator;
import com.zutubi.validation.validators.RequiredValidator;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ConfigurationValidatorProviderTest extends ZutubiTestCase
{
    private static final List<Validator> DELEGATE_VALIDATORS = Arrays.<Validator>asList(new EssentialValidator(), new RegexValidator(), new RequiredValidator());

    private ConfigurationValidatorProvider provider;
    private TestValidatorProvider delegateProvider;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        delegateProvider = new TestValidatorProvider();
        provider = new ConfigurationValidatorProvider();
        provider.setDelegates(Arrays.<ValidatorProvider>asList(delegateProvider));
    }

    public void testAllValidators()
    {
        helper(createContext(false), DELEGATE_VALIDATORS, DELEGATE_VALIDATORS);
    }

    public void testNoRequiredValidators()
    {
        List<Validator> expected = CollectionUtils.filter(DELEGATE_VALIDATORS, new Predicate<Validator>()
        {
            public boolean satisfied(Validator validator)
            {
                return !(validator instanceof RequiredValidator);
            }
        });

        helper(createContext(true), expected, DELEGATE_VALIDATORS);
    }

    public void testNonIgnorableRequiredValidator()
    {
        RequiredValidator nonIgnorable = new RequiredValidator();
        nonIgnorable.setIgnorable(false);

        List<Validator> expected = CollectionUtils.filter(DELEGATE_VALIDATORS, new Predicate<Validator>()
        {
            public boolean satisfied(Validator validator)
            {
                return !(validator instanceof RequiredValidator);
            }
        });
        expected.add(nonIgnorable);

        List<Validator> delegate = new LinkedList<Validator>(DELEGATE_VALIDATORS);
        delegate.add(nonIgnorable);

        helper(createContext(true), expected, delegate);
    }

    private void helper(ConfigurationValidationContext context, List<Validator> expected, List<Validator> delegate)
    {
        delegateProvider.setValidators(delegate);
        assertEquals(expected, provider.getValidators(null, context));
    }

    private ConfigurationValidationContext createContext(boolean template)
    {
        return new ConfigurationValidationContext(new A(), new MessagesTextProvider(this), null, null, template, false, null);
    }

    private static class A extends AbstractConfiguration
    {
    }

    private static class TestValidatorProvider implements ValidatorProvider
    {
        private List<Validator> validators;

        public List<Validator> getValidators(Object obj, ValidationContext context)
        {
            return validators;
        }

        public void setValidators(List<Validator> validators)
        {
            this.validators = validators;
        }
    }
}
