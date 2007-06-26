package com.zutubi.validation.providers;

import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.Validator;
import com.zutubi.validation.ValidatorProvider;
import com.zutubi.validation.validators.ValidateableValidator;

import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class ReflectionValidatorProvider implements ValidatorProvider
{
    public List<Validator> getValidators(Object obj, ValidationContext context)
    {
        List<Validator> validators = new LinkedList<Validator>();
        if (Validateable.class.isAssignableFrom(obj.getClass()))
        {
            validators.add(new ValidateableValidator());
        }
        return validators;
    }
}
