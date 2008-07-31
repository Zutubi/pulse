package com.zutubi.validation.providers;

import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.Validator;
import com.zutubi.validation.ValidatorProvider;
import com.zutubi.validation.validators.ValidateableValidator;

import java.util.*;

/**
 * Provides a validator for classes that extend Validateable.
 */
public class ReflectionValidatorProvider implements ValidatorProvider
{
    private static final List<Validator> VALIDATEABLE_VALIDATOR = Arrays.<Validator>asList(new ValidateableValidator());

    private Map<Class, Boolean> cache = Collections.synchronizedMap(new HashMap<Class, Boolean>());

    public List<Validator> getValidators(Object obj, ValidationContext context)
    {
        Class clazz = obj.getClass();
        Boolean isValidateable = cache.get(clazz);
        if(isValidateable == null)
        {
            isValidateable = Validateable.class.isAssignableFrom(clazz);
            cache.put(clazz, isValidateable);
        }

        if (isValidateable)
        {
            return VALIDATEABLE_VALIDATOR;
        }
        else
        {
            return Collections.emptyList();
        }
    }
}
