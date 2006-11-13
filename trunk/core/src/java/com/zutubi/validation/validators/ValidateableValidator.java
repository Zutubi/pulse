package com.zutubi.validation.validators;

import com.zutubi.validation.Validateable;
import com.zutubi.validation.ShortCircuitableValidator;

/**
 * <class-comment/>
 */
public class ValidateableValidator extends ValidatorSupport
{
    public void validate(Object obj)
    {
        if (Validateable.class.isAssignableFrom(obj.getClass()))
        {
            ((Validateable)obj).validate(getValidationContext());
        }
    }
}
