package com.zutubi.tove.config;

import com.zutubi.validation.Validator;
import com.zutubi.validation.ValidatorProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * A validator provider that adds capabilities customised to the
 * configuration subsystem.
 */
public class ConfigurationValidatorProvider implements ValidatorProvider
{
    private List<ValidatorProvider> delegates = new ArrayList<>();

    @Override
    public List<Validator> getValidators(Class clazz)
    {
        List<Validator> validators = new ArrayList<>();
        for (ValidatorProvider delegate: delegates)
        {
            validators.addAll(delegate.getValidators(clazz));
        }

        return validators;
    }

    public void setDelegates(List<ValidatorProvider> delegates)
    {
        this.delegates = delegates;
    }
}
