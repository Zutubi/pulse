package com.zutubi.validation;

import com.zutubi.validation.validators.DelegateValidator;
import com.zutubi.validation.validators.ValidateableValidator;

import java.util.LinkedList;
import java.util.List;

/**
 * A default implementation that obtains validators from a list of providers and applies them.
 */
public class DefaultValidationManager implements ValidationManager
{
    private List<ValidatorProvider> providers = new LinkedList<>();

    public DefaultValidationManager()
    {
    }

    public void validate(Object o, ValidationContext context) throws ValidationException
    {
        verify(providers);

        List<Validator> validators = new LinkedList<>();
        for (ValidatorProvider provider : providers)
        {
            validators.addAll(provider.getValidators(o.getClass()));
        }

        for (Validator v : validators)
        {
            if (context.shouldIgnoreValidator(v))
            {
                continue;
            }

            // short circuit is on a per field basis.
            if (v instanceof FieldValidator)
            {
                FieldValidator vf = (FieldValidator) v;
                if (context.hasFieldError(vf.getFieldName()) && (v instanceof ShortCircuitableValidator) && ((ShortCircuitableValidator)v).isShortCircuit())
                {
                    continue;
                }
            }

            if (v instanceof DelegateValidator)
            {
                //TODO: need to fix this, either by wiring or supporting static access to the validation manager.
                ((DelegateValidator)v).setValidationManager(this);
            }
            if (v instanceof ValidateableValidator && context.hasErrors() && ((ShortCircuitableValidator)v).isShortCircuit())
            {
                continue;
            }
            v.setValidationContext(context);
            v.validate(o);
        }
    }

    private void verify(List<ValidatorProvider> providers) throws IllegalArgumentException
    {
        if (providers == null || providers.size() == 0)
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set the validator providers. These are used to determine which validators should be applied to
     * the objects being validated.
     *
     * @param providers
     *
     * @throws IllegalArgumentException
     */
    public void setProviders(List<ValidatorProvider> providers)
    {
        verify(providers);
        this.providers = providers;
    }

    /**
     * Add a validator provider to the existing list of validator providers.
     * @param provider
     */
    public void addValidatorProvider(ValidatorProvider provider)
    {
        this.providers.add(provider);
    }
}
