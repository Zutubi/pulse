package com.zutubi.validation.providers;

import com.zutubi.validation.ValidatorProvider;
import com.zutubi.validation.Validator;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.validators.ValidateableValidator;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class ReflectionValidatorProvider implements ValidatorProvider
{
    public List<Validator> getValidators(Object obj)
    {
        List<Validator> validators = new LinkedList<Validator>();
        if (Validateable.class.isAssignableFrom(obj.getClass()))
        {
            validators.add(new ValidateableValidator());
        }
        return validators;
    }
}
