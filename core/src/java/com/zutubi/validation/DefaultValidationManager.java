package com.zutubi.validation;

import com.zutubi.validation.validators.DelegateValidator;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class DefaultValidationManager implements ValidationManager
{
    private List<ValidatorProvider> providers = new LinkedList<ValidatorProvider>();

    public DefaultValidationManager()
    {
    }

    public void validate(Object o) throws ValidationException
    {
        validate(o, new DelegatingValidationContext(o));
    }

    public void validate(Object o, ValidationContext context) throws ValidationException
    {
        verify(providers);

        // get validators
        List<Validator> validators = new LinkedList<Validator>();
        for (ValidatorProvider provider : providers)
        {
            validators.addAll(provider.getValidators(o));
        }

        // run them.
        for (Validator v : validators)
        {
            // short curcuit is on a per field basis.
            if (v instanceof FieldValidator)
            {
                FieldValidator vf = (FieldValidator) v;
                if (context.hasFieldError(vf.getFieldName()) && (v instanceof ShortCircuitableValidator) && ((ShortCircuitableValidator)v).isShortCircuit())
                {
                    // short circuit.
                    continue;
                }
            }

            if (v instanceof DelegateValidator)
            {
                //TODO: need to fix this, either by wiring or supporting static access to the validation manager.
                ((DelegateValidator)v).setValidationManager(this);
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
